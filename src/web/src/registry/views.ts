// Runtime registry of views. Views register themselves here via
// defineAsyncComponent in their own modules. This lets the router resolve
// routes at runtime without statically referencing view files that do not
// exist yet (they are created by later chunks).

const registry: Record<string, () => Promise<unknown>> = {};

export function registerView(
  key: string,
  factory: () => Promise<unknown>,
): void {
  registry[key] = factory;
}

export function resolveView(key: string): () => Promise<unknown> {
  return () => {
    const factory = registry[key];
    if (!factory) {
      // No view registered yet (or unknown key): render empty placeholder.
      return Promise.resolve({});
    }
    return Promise.resolve(factory());
  };
}
