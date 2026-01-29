import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Row, Col, Card, Statistic, Spin, Alert } from 'antd'
import {
  ShoppingOutlined,
  FileTextOutlined,
  QuestionCircleOutlined,
  MessageOutlined,
  YoutubeOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons'
import { dashboardApi } from '../api'
import { useAuth } from '../contexts/AuthContext'
import type { DashboardStats } from '../types'

function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const { user, isSuperAdmin } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const response = await dashboardApi.getStats()
        if (response.success && response.data) {
          setStats(response.data)
        } else {
          setError(response.message || '데이터를 불러오는데 실패했습니다.')
        }
      } catch {
        setError('데이터를 불러오는데 실패했습니다.')
      } finally {
        setLoading(false)
      }
    }

    fetchStats()
  }, [])

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 50 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (error) {
    return <Alert type="error" message={error} showIcon />
  }

  // 슈퍼관리자인데 회사가 선택되지 않은 경우
  if (isSuperAdmin && !user?.companyId) {
    return (
      <Alert
        type="info"
        message="회사를 선택해주세요"
        description="슈퍼관리자는 먼저 관리할 회사를 선택해야 합니다."
        action={
          <a onClick={() => navigate('/select-company')}>회사 선택하기</a>
        }
        showIcon
      />
    )
  }

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">대시보드</h1>
        {stats?.company && (
          <span style={{ color: '#666' }}>{stats.company.name}</span>
        )}
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card className="stat-card dashboard-card">
            <Statistic
              title="상품"
              value={stats?.productCount || 0}
              prefix={<ShoppingOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card className="stat-card dashboard-card">
            <Statistic
              title="게시글"
              value={stats?.postCount || 0}
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card className="stat-card dashboard-card">
            <Statistic
              title="FAQ"
              value={stats?.faqCount || 0}
              prefix={<QuestionCircleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card className="stat-card dashboard-card">
            <Statistic
              title="QnA"
              value={stats?.qnaCount || 0}
              prefix={<MessageOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card className="stat-card dashboard-card">
            <Statistic
              title="답변 대기"
              value={stats?.pendingQnaCount || 0}
              prefix={<ExclamationCircleOutlined />}
              valueStyle={{ color: stats?.pendingQnaCount ? '#cf1322' : undefined }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card className="stat-card dashboard-card">
            <Statistic
              title="유튜브"
              value={stats?.youtubeCount || 0}
              prefix={<YoutubeOutlined />}
            />
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default DashboardPage
