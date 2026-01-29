import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './contexts/AuthContext'
import AdminLayout from './components/AdminLayout'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import ProductListPage from './pages/products/ProductListPage'
import ProductFormPage from './pages/products/ProductFormPage'
import BoardListPage from './pages/boards/BoardListPage'
import PostListPage from './pages/posts/PostListPage'
import PostFormPage from './pages/posts/PostFormPage'
import FaqListPage from './pages/faqs/FaqListPage'
import FaqFormPage from './pages/faqs/FaqFormPage'
import QnaListPage from './pages/qnas/QnaListPage'
import QnaAnswerPage from './pages/qnas/QnaAnswerPage'
import YoutubeListPage from './pages/youtube/YoutubeListPage'
import YoutubeFormPage from './pages/youtube/YoutubeFormPage'
import SelectCompanyPage from './pages/SelectCompanyPage'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, loading } = useAuth()

  if (loading) {
    return <div>Loading...</div>
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="select-company" element={<SelectCompanyPage />} />
        <Route path="products" element={<ProductListPage />} />
        <Route path="products/new" element={<ProductFormPage />} />
        <Route path="products/:id/edit" element={<ProductFormPage />} />
        <Route path="boards" element={<BoardListPage />} />
        <Route path="boards/:boardId/posts" element={<PostListPage />} />
        <Route path="boards/:boardId/posts/new" element={<PostFormPage />} />
        <Route path="boards/:boardId/posts/:id/edit" element={<PostFormPage />} />
        <Route path="faqs" element={<FaqListPage />} />
        <Route path="faqs/new" element={<FaqFormPage />} />
        <Route path="faqs/:id/edit" element={<FaqFormPage />} />
        <Route path="qnas" element={<QnaListPage />} />
        <Route path="qnas/:id/answer" element={<QnaAnswerPage />} />
        <Route path="youtube" element={<YoutubeListPage />} />
        <Route path="youtube/new" element={<YoutubeFormPage />} />
        <Route path="youtube/:id/edit" element={<YoutubeFormPage />} />
      </Route>
    </Routes>
  )
}

export default App
