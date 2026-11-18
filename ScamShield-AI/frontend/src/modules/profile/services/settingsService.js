import { profileRequest } from "./profileService.js";

export const settingsService = {
  get: () => profileRequest("/api/settings"),
  update: (data) => profileRequest("/api/settings", { method: "PUT", body: data }),
};
