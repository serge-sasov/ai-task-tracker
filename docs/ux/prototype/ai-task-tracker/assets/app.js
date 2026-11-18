/* ai-task-tracker UX prototype — shared fixtures + helpers (U-02, T-005)
   Offline: no network, no storage, no external assets. Fixtures: docs/analysis/e2e-test-data-ai-task-tracker.yaml */
var APP = (function () {
  "use strict";

  var TASKS = [
    {
      id: "TASK-001", title: "Настройка CI для репозитория ai-task-tracker",
      date: "2026-11-10", start: "09:00", end: "11:30", duration: "2ч 30мин",
      status: "Завершена",
      actions: [
        ["09:05", "Анализ структуры репозитория и требований к CI"],
        ["09:20", "Предложены варианты конфигурации CI: GitLab CI, GitHub Actions"],
        ["10:05", "Пользователь выбрал вариант GitLab CI"],
        ["10:40", "Сгенерирован .gitlab-ci.yml (stages: lint, test, build)"],
        ["11:10", "Локальная проверка пайплайна"],
        ["11:25", "Задача завершена"]
      ],
      solutions: { proposed: ["GitLab CI", "GitHub Actions"], chosen: "GitLab CI" },
      usage: [
        { model: "qwen3.8:27b", input: 7200, output: 3000 },
        { model: "llama3.1:8b", input: 1400, output: 550 }
      ]
    },
    {
      id: "TASK-002", title: "Выбор способа авторизации локального API",
      date: "2026-11-10", start: "13:00", end: "15:00", duration: "2ч 00мин",
      status: "Завершена",
      actions: [
        ["13:05", "Описаны требования к авторизации локального API"],
        ["13:30", "Предложены варианты: JWT с ротацией, сессии в Redis"],
        ["14:10", "Пользователь выбрал JWT с ротацией"],
        ["14:50", "Задача завершена"]
      ],
      solutions: { proposed: ["JWT с ротацией", "Сессии в Redis"], chosen: "JWT с ротацией" },
      usage: null
    },
    {
      id: "TASK-003", title: "Исследование зависания backend под нагрузкой",
      date: "2026-11-11", start: "08:30", end: "10:45", duration: "2ч 15мин",
      status: "Прервана",
      actions: [
        ["08:35", "Воспроизведено зависание backend под нагрузкой"],
        ["08:50", "Определена причина: безограниченная очередь задач"],
        ["09:30", "Предложены варианты: ограничение очереди задач, пул соединений"],
        ["10:10", "Выбрано ограничение очереди задач, начата реализация"],
        ["10:45", "Сессия прервана (пользователь отошёл)"]
      ],
      solutions: { proposed: ["Ограничение очереди задач", "Пул соединений"], chosen: "Ограничение очереди задач" },
      usage: [
        { model: "qwen3.8:27b", input: 2000, output: 900 },
        { model: "llama3.1:8b", input: 400, output: 150 }
      ]
    },
    {
      id: "TASK-004", title: "Реализация векторного индекса для AI-поиска",
      date: "2026-11-11", start: "11:00", end: "12:10", duration: "1ч 10мин",
      status: "Не завершена",
      actions: [
        ["11:05", "Начата реализация векторного индекса"],
        ["11:40", "Протестированы семантические запросы"],
        ["12:00", "Обнаружено деградирование генерации сниппетов"],
        ["12:10", "Работа остановлена, задача не завершена"]
      ],
      solutions: { proposed: [], chosen: null },
      usage: [
        { model: "qwen3.8:27b", input: 1000, output: 400 },
        { model: "llama3.1:8b", input: 200, output: 70 }
      ]
    },
    {
      id: "TASK-005", title: "Добавление виджета токенов на экран статистики",
      date: "2026-11-11", start: "09:00", end: "09:40", duration: "40мин",
      status: "Завершена",
      actions: [
        ["09:05", "Добавлен виджет «Токены» на экран статистики"],
        ["09:25", "Проверена агрегация по моделям"],
        ["09:40", "Задача завершена"]
      ],
      solutions: { proposed: [], chosen: null },
      usage: [
        { model: "qwen3.8:27b", input: 550, output: 290 },
        { model: "llama3.1:8b", input: 100, output: 50 }
      ]
    }
  ];

  var SEARCH = [
    { query: "настройка CI для репозитория", hits: ["TASK-001", "TASK-004"] },
    { query: "авторизация и токены доступа", hits: ["TASK-002"] },
    { query: "зависание сервера под нагрузкой", hits: ["TASK-003"] }
  ];

  var TOKENS = {
    all_time: [
      { model: "qwen3.8:27b", input: 10750, output: 4590 },
      { model: "llama3.1:8b", input: 2100, output: 820 }
    ],
    "2026-11-10": [
      { model: "qwen3.8:27b", input: 7200, output: 3000 },
      { model: "llama3.1:8b", input: 1400, output: 550 }
    ],
    "2026-11-11": [
      { model: "qwen3.8:27b", input: 3550, output: 1590 },
      { model: "llama3.1:8b", input: 700, output: 270 }
    ]
  };

  function byId(id) {
    for (var i = 0; i < TASKS.length; i++) if (TASKS[i].id === id) return TASKS[i];
    return null;
  }

  function esc(s) {
    return String(s).replace(/[&<>"]/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c];
    });
  }

  /* State toolbar engine: buttons in .proto-toolbar[data-target] toggle
     .state-panel[data-state] blocks inside the target container. */
  function initToolbar(root) {
    var bar = root.querySelector(".proto-toolbar[data-target]");
    if (!bar) return;
    var container = root.querySelector(bar.getAttribute("data-target"));
    if (!container) return;
    bar.querySelectorAll("button[data-state]").forEach(function (btn) {
      btn.addEventListener("click", function () {
        container.querySelectorAll(".state-panel").forEach(function (p) {
          p.hidden = p.getAttribute("data-state") !== btn.getAttribute("data-state");
        });
        bar.querySelectorAll("button[data-state]").forEach(function (b) {
          b.setAttribute("aria-pressed", String(b === btn));
        });
      });
    });
  }

  function statusClass(status) {
    if (status === "Завершена") return "done";
    if (status === "Прервана") return "interrupted";
    return "open";
  }

  function statusHtml(status) {
    return '<span class="status ' + statusClass(status) + '"><span class="dot" aria-hidden="true"></span>' + esc(status) + "</span>";
  }

  function modelSummary(t) {
    var usage = t.usage || [];
    var models = usage.map(function (u) {
      return { name: u.model, total: (u.input || 0) + (u.output || 0) };
    });
    models.sort(function (a, b) {
      if (b.total !== a.total) return b.total - a.total;
      return a.name < b.name ? -1 : a.name > b.name ? 1 : 0;
    });
    var names = models.map(function (m) { return m.name; });
    return { primary: names[0] || "", more: names.length > 1 ? names.length - 1 : 0, list: names };
  }

  function taskRow(t) {
    var models = modelSummary(t);
    var modelCell;
    if (!models.primary) {
      modelCell = '<td><span class="model-none" title="Нет данных" aria-label="Модель: Нет данных">Нет данных</span></td>';
    } else {
      var label = "Модели: " + models.list.join(", ");
      modelCell = '<td><span class="model-cell" title="' + esc(label) + '" aria-label="' + esc(label) + '">' +
        esc(models.primary) + (models.more ? ' <span class="model-more">+' + models.more + "</span>" : "") +
        "</span></td>";
    }
    return '<tr><td>' + esc(t.id) + "</td>" +
      '<td><a href="uc-e2e-003.html">' + esc(t.title) + "</a></td>" +
      modelCell +
      "<td>" + esc(t.date) + "</td><td>" + esc(t.duration) + "</td>" +
      "<td>" + statusHtml(t.status) + "</td></tr>";
  }

  function renderTaskTable(el, tasks) {
    el.innerHTML =
      '<table class="table" aria-label="Список задач">' +
      "<thead><tr><th scope=\"col\">ID</th><th scope=\"col\">Название</th><th scope=\"col\">Модель</th><th scope=\"col\">Дата</th><th scope=\"col\">Продолжительность</th><th scope=\"col\">Статус</th></tr></thead>" +
      "<tbody>" + tasks.map(taskRow).join("") + "</tbody></table>";
  }

  document.addEventListener("DOMContentLoaded", function () {
    initToolbar(document);
  });

  return {
    TASKS: TASKS, SEARCH: SEARCH, TOKENS: TOKENS,
    byId: byId, esc: esc, statusHtml: statusHtml,
    renderTaskTable: renderTaskTable, initToolbar: initToolbar
  };
})();
