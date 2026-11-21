#requires -Version 5.1
[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [ValidateSet('start', 'status', 'wait', 'stop', 'tail', 'list', 'host', 'self-test')]
    [string]$Action = 'status',

    [string]$Name = '',
    [string]$FilePath = '',
    [string[]]$Arguments = @(),
    [string]$WorkDir = '',
    [int]$TimeoutSec = 600,
    [int]$HeartbeatSec = 10,
    [int]$WaitSec = 30,
    [int]$Lines = 20,
    [string]$LogPath = '',
    [string]$StateDir = '',
    [switch]$Json,
    [switch]$SelfTest
)

$ErrorActionPreference = 'Stop'
$script:Encoding = New-Object System.Text.UTF8Encoding($false)
if ([string]::IsNullOrWhiteSpace($StateDir)) {
    $StateDir = Join-Path (Get-Location).Path 'harness\run-state'
}
$StateDir = [System.IO.Path]::GetFullPath($StateDir)

function Write-Utf8NoBom {
    param([string]$Path, [string]$Text)
    $dir = Split-Path -Parent $Path
    if ($dir -and -not (Test-Path -LiteralPath $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
    [System.IO.File]::WriteAllText($Path, $Text, $script:Encoding)
}

function Write-JsonFile {
    param([string]$Path, $Object)
    Write-Utf8NoBom -Path $Path -Text (($Object | ConvertTo-Json -Depth 8) + "`r`n")
}

function Read-JsonFile {
    param([string]$Path)
    if (-not (Test-Path -LiteralPath $Path)) { return $null }
    return (Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json)
}

function Get-SafeName {
    param([string]$Value)
    $safe = ($Value -replace '[^A-Za-z0-9._-]', '-').Trim('-')
    if ([string]::IsNullOrWhiteSpace($safe)) { $safe = 'run' }
    return $safe
}

function Get-RunDir {
    param([string]$Value)
    return (Join-Path $StateDir (Get-SafeName $Value))
}

function Get-StatusPath {
    param([string]$Value)
    return (Join-Path (Get-RunDir $Value) 'status.json')
}

function Get-SpecPath {
    param([string]$Value)
    return (Join-Path (Get-RunDir $Value) 'spec.json')
}

function Get-LogPath {
    param([string]$Value)
    $status = Read-JsonFile (Get-StatusPath $Value)
    if ($status -and $status.log_path) { return [string]$status.log_path }
    return (Join-Path (Get-RunDir $Value) 'run.log')
}

function Get-Now {
    return (Get-Date).ToString('yyyy-MM-ddTHH:mm:ss')
}

function ConvertTo-ArgumentString {
    param([string[]]$Items)
    $parts = @()
    foreach ($item in @($Items)) {
        $value = [string]$item
        if ($value -match '[\s"]') {
            $parts += ('"' + ($value -replace '"', '\"') + '"')
        }
        else {
            $parts += $value
        }
    }
    return ($parts -join ' ')
}

function Stop-Tree {
    param([int]$ProcessId)
    if ($ProcessId -le 0) { return }
    try {
        & taskkill.exe /PID $ProcessId /T /F 2>$null | Out-Null
    }
    catch {
        Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
    }
}

function Read-NewText {
    param([string]$Path, [ref]$Position)
    if (-not (Test-Path -LiteralPath $Path)) { return '' }
    $stream = $null
    $reader = $null
    try {
        $stream = [System.IO.File]::Open($Path, [System.IO.FileMode]::Open, [System.IO.FileAccess]::Read, [System.IO.FileShare]::ReadWrite)
        if ($stream.Length -le $Position.Value) { return '' }
        $stream.Seek($Position.Value, [System.IO.SeekOrigin]::Begin) | Out-Null
        $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8, $true)
        $text = $reader.ReadToEnd()
        $Position.Value = $stream.Position
        return $text
    }
    catch {
        return ''
    }
    finally {
        if ($reader) { $reader.Dispose() }
        if ($stream) { $stream.Dispose() }
    }
}

function Add-LogChunk {
    param([System.IO.StreamWriter]$Writer, [string]$Text, [string]$Channel, [ref]$LastLine)
    if ([string]::IsNullOrEmpty($Text)) { return }
    $stamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    foreach ($line in ($Text -split "\r?\n")) {
        if ([string]::IsNullOrEmpty($line)) { continue }
        $Writer.WriteLine("$stamp [$Channel] $line")
        $LastLine.Value = $line
    }
    $Writer.Flush()
}

function Write-Status {
    param([string]$Value, $Status)
    $Status.elapsed_sec = [math]::Round(((Get-Date) - [datetime]::ParseExact([string]$Status.started_at, 'yyyy-MM-ddTHH:mm:ss', [System.Globalization.CultureInfo]::InvariantCulture)).TotalSeconds, 0)
    $Status.updated_at = Get-Now
    Write-JsonFile -Path (Get-StatusPath $Value) -Object $Status
}

function Start-MonitoredRun {
    param(
        [string]$Value,
        [string]$Target,
        [string[]]$TargetArgs,
        [string]$Directory,
        [int]$LimitSec,
        [int]$BeatSec,
        [string]$LogFile
    )

    if ([string]::IsNullOrWhiteSpace($Value)) { throw 'Name is required' }
    if ([string]::IsNullOrWhiteSpace($Target)) { throw 'FilePath is required' }
    if ([string]::IsNullOrWhiteSpace($Directory)) { $Directory = (Get-Location).Path }
    $Directory = (Resolve-Path -LiteralPath $Directory).Path
    $runDir = Get-RunDir $Value
    New-Item -ItemType Directory -Path $runDir -Force | Out-Null

    $existing = Read-JsonFile (Get-StatusPath $Value)
    if ($existing -and $existing.state -eq 'running') {
        $alive = $false
        if ($existing.target_pid) {
            $alive = [bool](Get-Process -Id ([int]$existing.target_pid) -ErrorAction SilentlyContinue)
        }
        if ($alive) { throw "run '$Value' is already running; stop it first" }
    }

    $fullLog = if ([string]::IsNullOrWhiteSpace($LogFile)) { Join-Path $runDir 'run.log' } else { [System.IO.Path]::GetFullPath($LogFile) }
    $logDir = Split-Path -Parent $fullLog
    if ($logDir -and -not (Test-Path -LiteralPath $logDir)) {
        New-Item -ItemType Directory -Path $logDir -Force | Out-Null
    }

    $spec = [ordered]@{
        name        = $Value
        file_path   = $Target
        arguments   = @($TargetArgs)
        work_dir    = $Directory
        timeout_sec = $LimitSec
        heartbeat_s = $BeatSec
        log_path    = $fullLog
        started_at  = Get-Now
    }
    Write-JsonFile -Path (Get-SpecPath $Value) -Object $spec

    $status = [ordered]@{
        name         = $Value
        state        = 'starting'
        host_pid     = $null
        target_pid   = $null
        started_at   = $spec.started_at
        finished_at  = $null
        exit_code    = $null
        timeout_sec  = $LimitSec
        log_path     = $fullLog
        log_bytes    = 0
        elapsed_sec  = 0
        heartbeat_at = $spec.started_at
        last_line    = ''
        updated_at   = $spec.started_at
    }
    Write-JsonFile -Path (Get-StatusPath $Value) -Object $status
    if (Test-Path -LiteralPath $fullLog) { Remove-Item -LiteralPath $fullLog -Force -ErrorAction SilentlyContinue }

    $command = "& '$($PSCommandPath -replace "'", "''")' -Action host -Name '$($Value -replace "'", "''")' -StateDir '$($StateDir -replace "'", "''")'"
    $encoded = [Convert]::ToBase64String([System.Text.Encoding]::Unicode.GetBytes($command))
    $hostProcess = Start-Process -FilePath 'powershell.exe' -ArgumentList @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-EncodedCommand', $encoded) -WindowStyle Hidden -PassThru
    $status.host_pid = $hostProcess.Id
    Write-Status -Value $Value -Status $status

    $deadline = (Get-Date).AddSeconds(3)
    do {
        Start-Sleep -Milliseconds 100
        $current = Read-JsonFile (Get-StatusPath $Value)
        if ($current -and $current.state -ne 'starting') { break }
    } while ((Get-Date) -lt $deadline)

    $current = Read-JsonFile (Get-StatusPath $Value)
    if ($current) {
        if ($Json) { Write-Host ($current | ConvertTo-Json -Depth 8 -Compress) }
        else { Write-Host ("started name={0} host_pid={1} log={2} status={3}" -f $Value, $current.host_pid, $current.log_path, (Get-StatusPath $Value)) }
        return 0
    }
    Write-Host ("started name={0} host_pid={1} log={2} status={3}" -f $Value, $hostProcess.Id, $fullLog, (Get-StatusPath $Value))
    return 0
}

function Invoke-Host {
    param([string]$Value)

    $spec = Read-JsonFile (Get-SpecPath $Value)
    if (-not $spec) { throw "spec not found for '$Value'" }
    $logFile = [string]$spec.log_path
    $outFile = Join-Path (Get-RunDir $Value) 'target.stdout.tmp'
    $errFile = Join-Path (Get-RunDir $Value) 'target.stderr.tmp'
    Remove-Item -LiteralPath $outFile, $errFile -Force -ErrorAction SilentlyContinue

    $status = [ordered]@{
        name         = $Value
        state        = 'running'
        host_pid     = $PID
        target_pid   = $null
        started_at   = [string]$spec.started_at
        finished_at  = $null
        exit_code    = $null
        timeout_sec  = [int]$spec.timeout_sec
        log_path     = $logFile
        log_bytes    = 0
        elapsed_sec  = 0
        heartbeat_at = Get-Now
        last_line    = ''
        updated_at   = Get-Now
    }

    $writer = New-Object System.IO.StreamWriter($logFile, $true, $script:Encoding)
    $writer.AutoFlush = $true
    $lastLine = ''
    $writer.WriteLine(("[{0}] [start] {1} {2}" -f (Get-Now), $spec.file_path, ((@($spec.arguments) -join ' ').Trim())))
    $writer.Flush()

    $exitFile = Join-Path (Get-RunDir $Value) 'target.exit.tmp'
    $targetSpecPath = Join-Path (Get-RunDir $Value) 'target.json'
    Remove-Item -LiteralPath $exitFile -Force -ErrorAction SilentlyContinue
    $targetSpec = [ordered]@{
        file_path = [string]$spec.file_path
        arguments = @($spec.arguments)
        work_dir  = [string]$spec.work_dir
        out_file  = $outFile
        err_file  = $errFile
        exit_file = $exitFile
    }
    Write-JsonFile -Path $targetSpecPath -Object $targetSpec

    $process = $null
    try {
        $childScript = Join-Path $PSScriptRoot 'run-monitored-child.ps1'
        if (-not (Test-Path -LiteralPath $childScript)) { throw "child runner not found: $childScript" }
        $escapedChild = $childScript -replace "'", "''"
        $escapedSpec = $targetSpecPath -replace "'", "''"
        $childCommand = "& '$escapedChild' -SpecPath '$escapedSpec'"
        $encodedChild = [Convert]::ToBase64String([System.Text.Encoding]::Unicode.GetBytes($childCommand))
        $process = Start-Process -FilePath 'powershell.exe' -ArgumentList @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-EncodedCommand', $encodedChild) -WindowStyle Hidden -PassThru
        $status.target_pid = $process.Id
        Write-Status -Value $Value -Status $status
    }
    catch {
        $status.state = 'error'
        $status.exit_code = 1
        $status.finished_at = Get-Now
        $writer.WriteLine(("[{0}] [error] {1}" -f (Get-Now), $_.Exception.Message))
        $writer.Flush()
        Write-Status -Value $Value -Status $status
        $writer.Dispose()
        return 1
    }

    $outPos = 0
    $errPos = 0
    $nextBeat = (Get-Date).AddSeconds([math]::Max(1, [int]$spec.heartbeat_s))
    $deadline = (Get-Date).AddSeconds([math]::Max(1, [int]$spec.timeout_sec))
    $exitCode = $null
    $finalState = 'exited'

    while ($true) {
        $outText = Read-NewText -Path $outFile -Position ([ref]$outPos)
        $errText = Read-NewText -Path $errFile -Position ([ref]$errPos)
        Add-LogChunk -Writer $writer -Text $outText -Channel 'out' -LastLine ([ref]$lastLine)
        Add-LogChunk -Writer $writer -Text $errText -Channel 'err' -LastLine ([ref]$lastLine)

        if ($process.HasExited) {
            $outText = Read-NewText -Path $outFile -Position ([ref]$outPos)
            $errText = Read-NewText -Path $errFile -Position ([ref]$errPos)
            Add-LogChunk -Writer $writer -Text $outText -Channel 'out' -LastLine ([ref]$lastLine)
            Add-LogChunk -Writer $writer -Text $errText -Channel 'err' -LastLine ([ref]$lastLine)
            if (Test-Path -LiteralPath $exitFile) {
                $exitCode = [int]((Get-Content -LiteralPath $exitFile -Raw).Trim())
            }
            $finalState = 'exited'
            if ($null -eq $exitCode) {
                $finalState = 'error'
                $exitCode = 1
            }
            break
        }

        if ((Get-Date) -ge $deadline) {
            $finalState = 'timeout'
            $exitCode = 124
            $writer.WriteLine(("[{0}] [timeout] elapsed={1}s; killing process tree {2}" -f (Get-Now), [int]$spec.timeout_sec, $process.Id))
            $writer.Flush()
            Stop-Tree -ProcessId $process.Id
            break
        }

        if ((Get-Date) -ge $nextBeat) {
            $writer.WriteLine(("[{0}] [heartbeat] elapsed={1}s pid={2}" -f (Get-Now), [int]((Get-Date) - [datetime]::ParseExact([string]$spec.started_at, 'yyyy-MM-ddTHH:mm:ss', [System.Globalization.CultureInfo]::InvariantCulture)).TotalSeconds, $process.Id))
            $writer.Flush()
            $status.heartbeat_at = Get-Now
            $status.last_line = $lastLine
            if (Test-Path -LiteralPath $logFile) { $status.log_bytes = (Get-Item -LiteralPath $logFile).Length }
            Write-Status -Value $Value -Status $status
            $nextBeat = (Get-Date).AddSeconds([math]::Max(1, [int]$spec.heartbeat_s))
        }

        Start-Sleep -Milliseconds 500
    }

    $writer.WriteLine(("[{0}] [{1}] exit_code={2}" -f (Get-Now), $finalState, $exitCode))
    $writer.Flush()
    $status.state = $finalState
    $status.exit_code = $exitCode
    $status.finished_at = Get-Now
    $status.last_line = $lastLine
    if (Test-Path -LiteralPath $logFile) { $status.log_bytes = (Get-Item -LiteralPath $logFile).Length }
    Write-Status -Value $Value -Status $status
    $writer.Dispose()
    Remove-Item -LiteralPath $outFile, $errFile -Force -ErrorAction SilentlyContinue
    return 0
}

function Get-MonitoredStatus {
    param([string]$Value)
    $status = Read-JsonFile (Get-StatusPath $Value)
    if (-not $status) { return $null }
    if ($status.state -eq 'running') {
        $targetAlive = $false
        if ($status.target_pid) {
            $targetAlive = [bool](Get-Process -Id ([int]$status.target_pid) -ErrorAction SilentlyContinue)
        }
        if (-not $targetAlive) { $status | Add-Member -NotePropertyName target_alive -NotePropertyValue $false -Force }
        else { $status | Add-Member -NotePropertyName target_alive -NotePropertyValue $true -Force }
    }
    return $status
}

function Show-MonitoredStatus {
    param([string]$Value)
    $status = Get-MonitoredStatus $Value
    if (-not $status) {
        Write-Host "status not found for '$Value'"
        return 1
    }
    if ($Json) {
        Write-Host ($status | ConvertTo-Json -Depth 8 -Compress)
        return 0
    }
    $exit = if ($null -ne $status.exit_code) { [string]$status.exit_code } else { '-' }
    $alive = if ($status.PSObject.Properties['target_alive']) { [string]$status.target_alive } else { '-' }
    Write-Host ("name={0} state={1} target_pid={2} target_alive={3} elapsed={4}s exit={5} log={6} bytes={7}" -f $status.name, $status.state, $status.target_pid, $alive, $status.elapsed_sec, $exit, $status.log_path, $status.log_bytes)
    if ($status.last_line) { Write-Host ("last=" + $status.last_line) }
    return 0
}

function Wait-Monitored {
    param([string]$Value, [int]$Seconds)
    $deadline = (Get-Date).AddSeconds([math]::Max(1, $Seconds))
    do {
        $status = Get-MonitoredStatus $Value
        if (-not $status) {
            Write-Host "status not found for '$Value'"
            return 2
        }
        if ($status.state -in @('exited', 'timeout', 'stopped', 'error')) {
            Show-MonitoredStatus -Value $Value | Out-Null
            return 0
        }
        Start-Sleep -Milliseconds 500
    } while ((Get-Date) -lt $deadline)
    Show-MonitoredStatus -Value $Value | Out-Null
    return 124
}

function Stop-Monitored {
    param([string]$Value)
    $status = Read-JsonFile (Get-StatusPath $Value)
    if (-not $status) { throw "status not found for '$Value'" }
    if ($status.target_pid) { Stop-Tree -ProcessId ([int]$status.target_pid) }
    if ($status.host_pid -and [int]$status.host_pid -ne $PID) { Stop-Tree -ProcessId ([int]$status.host_pid) }
    $status.state = 'stopped'
    $status.finished_at = Get-Now
    if ($null -eq $status.exit_code) { $status.exit_code = 130 }
    Write-Status -Value $Value -Status $status
    Show-MonitoredStatus -Value $Value | Out-Null
    return 0
}

function Show-MonitoredTail {
    param([string]$Value, [int]$Count)
    $log = Get-LogPath $Value
    if (-not (Test-Path -LiteralPath $log)) { throw "log not found for '$Value': $log" }
    Get-Content -LiteralPath $log -Tail ([math]::Max(1, $Count)) -Encoding UTF8
    return 0
}

function Show-MonitoredList {
    if (-not (Test-Path -LiteralPath $StateDir)) {
        Write-Host 'no monitored runs'
        return 0
    }
    $found = $false
    foreach ($dir in (Get-ChildItem -LiteralPath $StateDir -Directory -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending)) {
        $status = Read-JsonFile (Join-Path $dir.FullName 'status.json')
        if (-not $status) { continue }
        $found = $true
        $exit = if ($null -ne $status.exit_code) { [string]$status.exit_code } else { '-' }
        Write-Host ("{0} state={1} pid={2} elapsed={3}s exit={4} log={5}" -f $status.name, $status.state, $status.target_pid, $status.elapsed_sec, $exit, $status.log_path)
    }
    if (-not $found) { Write-Host 'no monitored runs' }
    return 0
}

function Invoke-SelfTest {
    $temp = Join-Path ([System.IO.Path]::GetTempPath()) ("run-monitored-selftest-" + [guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory -Path $temp -Force | Out-Null
    $script:StateDir = $temp
    try {
        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        Start-MonitoredRun -Value 'fastfail' -Target 'powershell.exe' -TargetArgs @('-NoProfile', '-Command', 'Write-Output "boom"; exit 7') -Directory $temp -LimitSec 30 -BeatSec 5 -LogFile (Join-Path $temp 'fastfail.log') | Out-Null
        $sw.Stop()
        if ($sw.Elapsed.TotalSeconds -gt 5) { throw "start took $($sw.Elapsed.TotalSeconds)s (>5s)" }

        $deadline = (Get-Date).AddSeconds(15)
        do {
            $fast = Read-JsonFile (Get-StatusPath 'fastfail')
            if ($fast.state -in @('exited', 'error', 'timeout')) { break }
            Start-Sleep -Milliseconds 250
        } while ((Get-Date) -lt $deadline)
        $fast = Read-JsonFile (Get-StatusPath 'fastfail')
        if ($fast.state -ne 'exited') { throw "fastfail state=$($fast.state)" }
        if ([int]$fast.exit_code -ne 7) { throw "fastfail exit_code=$($fast.exit_code)" }
        $tail = Get-Content -LiteralPath $fast.log_path -Raw -Encoding UTF8
        if ($tail -notmatch 'boom') { throw 'fastfail log does not contain boom' }

        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        Start-MonitoredRun -Value 'sleeper' -Target 'powershell.exe' -TargetArgs @('-NoProfile', '-Command', 'Start-Sleep -Seconds 60') -Directory $temp -LimitSec 60 -BeatSec 2 -LogFile (Join-Path $temp 'sleeper.log') | Out-Null
        $sw.Stop()
        if ($sw.Elapsed.TotalSeconds -gt 5) { throw "sleeper start took $($sw.Elapsed.TotalSeconds)s (>5s)" }
        Start-Sleep -Milliseconds 500
        $sleeper = Read-JsonFile (Get-StatusPath 'sleeper')
        if ($sleeper.state -ne 'running') { throw "sleeper state=$($sleeper.state)" }
        $waitCode = Wait-Monitored -Value 'sleeper' -Seconds 2
        if ($waitCode -ne 124) { throw "sleeper wait should time out with 124, got $waitCode" }
        Stop-Monitored -Value 'sleeper' | Out-Null
        Start-Sleep -Milliseconds 500
        $stopped = Read-JsonFile (Get-StatusPath 'sleeper')
        if ($stopped.state -ne 'stopped') { throw "sleeper state after stop=$($stopped.state)" }
        if ($stopped.target_pid -and (Get-Process -Id ([int]$stopped.target_pid) -ErrorAction SilentlyContinue)) {
            throw 'sleeper target process is still alive after stop'
        }

        Write-Host 'SELFTEST PASS'
        return 0
    }
    catch {
        Write-Host "SELFTEST FAIL: $($_.Exception.Message)"
        return 1
    }
    finally {
        if (Test-Path -LiteralPath $temp) {
            Remove-Item -LiteralPath $temp -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
}

if ($SelfTest -or $Action -eq 'self-test') {
    exit (Invoke-SelfTest)
}

switch ($Action) {
    'start' {
        if ([string]::IsNullOrWhiteSpace($Name)) { throw 'Name is required' }
        exit (Start-MonitoredRun -Value $Name -Target $FilePath -TargetArgs $Arguments -Directory $WorkDir -LimitSec $TimeoutSec -BeatSec $HeartbeatSec -LogFile $LogPath)
    }
    'host' {
        if ([string]::IsNullOrWhiteSpace($Name)) { throw 'Name is required' }
        exit (Invoke-Host -Value $Name)
    }
    'status' {
        if ([string]::IsNullOrWhiteSpace($Name)) { throw 'Name is required' }
        exit (Show-MonitoredStatus -Value $Name)
    }
    'wait' {
        if ([string]::IsNullOrWhiteSpace($Name)) { throw 'Name is required' }
        $code = Wait-Monitored -Value $Name -Seconds $WaitSec
        exit $code
    }
    'stop' {
        if ([string]::IsNullOrWhiteSpace($Name)) { throw 'Name is required' }
        exit (Stop-Monitored -Value $Name)
    }
    'tail' {
        if ([string]::IsNullOrWhiteSpace($Name)) { throw 'Name is required' }
        exit (Show-MonitoredTail -Value $Name -Count $Lines)
    }
    'list' {
        exit (Show-MonitoredList)
    }
}
