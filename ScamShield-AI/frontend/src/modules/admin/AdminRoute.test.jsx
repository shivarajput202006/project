import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import AdminRoute from "./AdminRoute.jsx";
import { useAuth } from "../auth/AuthContext.jsx";

vi.mock("../auth/AuthContext.jsx", () => ({ useAuth: vi.fn() }));

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

function renderAdminRoute(auth, path = "/admin") {
  useAuth.mockReturnValue(auth);
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route element={<AdminRoute />}>
          <Route path="/admin" element={<p>Admin content</p>} />
        </Route>
        <Route path="/login" element={<p>Login page</p>} />
        <Route path="/dashboard" element={<p>User dashboard</p>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe("AdminRoute", () => {
  it("shows protected content to an authenticated administrator", () => {
    renderAdminRoute({ user: { role: "ADMIN" }, loading: false });

    expect(screen.getByText("Admin content")).toBeInTheDocument();
  });

  it("redirects unauthenticated users to login", () => {
    renderAdminRoute({ user: null, loading: false });

    expect(screen.getByText("Login page")).toBeInTheDocument();
  });

  it("redirects non-administrators to the user dashboard", () => {
    renderAdminRoute({ user: { role: "USER" }, loading: false });

    expect(screen.getByText("User dashboard")).toBeInTheDocument();
  });

  it("does not render protected content while authentication is loading", () => {
    renderAdminRoute({ user: null, loading: true });

    expect(screen.getByRole("status")).toHaveTextContent("Checking your administrator access");
    expect(screen.queryByText("Admin content")).not.toBeInTheDocument();
  });
});
