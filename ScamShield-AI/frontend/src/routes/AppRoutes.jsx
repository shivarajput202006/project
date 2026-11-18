import { lazy, Suspense } from "react";
import { Route, Routes } from "react-router-dom";
import AdminRoute from "../modules/admin/AdminRoute.jsx";
import ProtectedRoute from "../modules/auth/ProtectedRoute.jsx";
import PublicOnlyRoute from "../modules/auth/PublicOnlyRoute.jsx";

const LandingPage = lazy(() => import("../modules/landing/LandingPage.jsx"));
const AuthLayout = lazy(() => import("../modules/auth/AuthLayout.jsx"));
const LoginPage = lazy(() => import("../modules/auth/LoginPage.jsx"));
const RegisterPage = lazy(() => import("../modules/auth/RegisterPage.jsx"));
const ForgotPasswordPage = lazy(() => import("../modules/auth/ForgotPasswordPage.jsx"));
const DashboardPage = lazy(() => import("../modules/auth/DashboardPage.jsx"));
const ComingSoonPage = lazy(() => import("../modules/dashboard/ComingSoonPage.jsx"));
const ScamDetectorPage = lazy(() => import("../modules/scam-detector/ScamDetectorPage.jsx"));
const URLScannerPage = lazy(() => import("../modules/url-scanner/URLScannerPage.jsx"));
const JobFinderPage = lazy(() => import("../modules/job-finder/JobFinderPage.jsx"));
const ResumeAnalyzerPage = lazy(() => import("../modules/resume-analyzer/ResumeAnalyzerPage.jsx"));
const JobMatchingPage = lazy(() => import("../modules/job-matching/JobMatchingPage.jsx"));
const ReportsPage = lazy(() => import("../modules/reports/ReportsPage.jsx"));
const ReportDetailsPage = lazy(() => import("../modules/reports/ReportDetailsPage.jsx"));
const ProfilePage = lazy(() => import("../modules/profile/ProfilePage.jsx"));
const SettingsPage = lazy(() => import("../modules/profile/SettingsPage.jsx"));
const PrivacyPolicyPage = lazy(() => import("../modules/profile/PrivacyPolicyPage.jsx"));
const NotificationsPage = lazy(() => import("../modules/notifications/NotificationsPage.jsx"));
const AssistantPage = lazy(() => import("../modules/assistant/AssistantPage.jsx"));
const AdminDashboard = lazy(() => import("../modules/admin/AdminDashboard.jsx"));

export default function AppRoutes() {
  return (
    <Suspense fallback={<div className="route-loading" role="status">Loading page...</div>}>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/privacy-policy" element={<PrivacyPolicyPage />} />
        <Route element={<AuthLayout />}>
          <Route element={<PublicOnlyRoute />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
          </Route>
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        </Route>
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/scam-detector" element={<ScamDetectorPage />} />
          <Route path="/url-scanner" element={<URLScannerPage />} />
          <Route path="/live-jobs" element={<JobFinderPage />} />
          <Route path="/resume-analyzer" element={<ResumeAnalyzerPage />} />
          <Route path="/job-matching" element={<JobMatchingPage />} />
          <Route path="/reports" element={<ReportsPage />} />
          <Route path="/reports/:id" element={<ReportDetailsPage />} />
          <Route path="/scam-reports" element={<ReportsPage />} />
          <Route path="/safety-assistant" element={<ComingSoonPage />} />
          <Route path="/saved-jobs" element={<JobFinderPage />} />
          <Route path="/notifications" element={<NotificationsPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/settings" element={<SettingsPage />} />
          <Route path="/dashboard/assistant" element={<AssistantPage />} />
        </Route>
        <Route element={<AdminRoute />}>
          <Route path="/admin" element={<AdminDashboard />} />
        </Route>
        <Route path="*" element={<LandingPage />} />
      </Routes>
    </Suspense>
  );
}
