import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Sidebar from "./components/Sidebar";
import MaterialsPage from "./pages/MaterialsPage";
import LotsPage from "./pages/LotsPage";
import QCTestsPage from "./pages/QCTestsPage";
import BatchesPage from "./pages/BatchesPage";
import LabelsPage from "./pages/LabelsPage";
import DashboardPage from "./pages/DashboardPage";
import UsersPage from "./pages/UsersPage";

export default function App() {
  return (
    <BrowserRouter>
      <div className="app-layout">
        <h1>Hello World 12345
        <Sidebar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Navigate to="/materials" replace />} />
            <Route path="/materials" element={<MaterialsPage />} />
            <Route path="/lots" element={<LotsPage />} />
            <Route path="/qctests" element={<QCTestsPage />} />
            <Route path="/batches" element={<BatchesPage />} />
            <Route path="/labels" element={<LabelsPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/users" element={<UsersPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}
