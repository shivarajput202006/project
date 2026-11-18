import { afterEach, describe, expect, it, vi } from "vitest";
import { adminService } from "./adminService.js";

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("adminService", () => {
  it("requests real overview data with cookie credentials", async () => {
    const overview = { totalUsers: 4, totalScamAnalyses: 9 };
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => overview,
    });
    vi.stubGlobal("fetch", fetchMock);

    await expect(adminService.getOverview()).resolves.toEqual(overview);
    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringContaining("/api/admin/dashboard/overview"),
      expect.objectContaining({ credentials: "include" }),
    );
  });

  it("preserves authorization errors for callers", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: false,
      status: 403,
      json: async () => ({ message: "Administrator access is required." }),
    }));

    await expect(adminService.getOverview()).rejects.toEqual(
      expect.objectContaining({
        name: "AdminApiError",
        message: "Administrator access is required.",
        status: 403,
      }),
    );
  });
});
