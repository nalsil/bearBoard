import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Card, List, Spin, Alert, Avatar, message } from 'antd'
import { ShopOutlined } from '@ant-design/icons'
import { companiesApi } from '../api'
import { useAuth } from '../contexts/AuthContext'
import type { Company } from '../types'

function SelectCompanyPage() {
  const [companies, setCompanies] = useState<Company[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const { isSuperAdmin, switchCompany } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    if (!isSuperAdmin) {
      message.error('슈퍼관리자만 접근할 수 있습니다.')
      navigate('/dashboard')
      return
    }

    const fetchCompanies = async () => {
      try {
        const response = await companiesApi.getActiveList()
        if (response.success && response.data) {
          setCompanies(response.data)
        } else {
          setError(response.message || '회사 목록을 불러오는데 실패했습니다.')
        }
      } catch {
        setError('회사 목록을 불러오는데 실패했습니다.')
      } finally {
        setLoading(false)
      }
    }

    fetchCompanies()
  }, [isSuperAdmin, navigate])

  const handleSelectCompany = async (companyId: number) => {
    const success = await switchCompany(companyId)
    if (success) {
      navigate('/dashboard')
    }
  }

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

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">회사 선택</h1>
      </div>

      <Card>
        <List
          itemLayout="horizontal"
          dataSource={companies}
          renderItem={(company) => (
            <List.Item
              style={{ cursor: 'pointer' }}
              onClick={() => handleSelectCompany(company.id)}
            >
              <List.Item.Meta
                avatar={
                  company.logoUrl ? (
                    <Avatar src={company.logoUrl} size={48} />
                  ) : (
                    <Avatar icon={<ShopOutlined />} size={48} />
                  )
                }
                title={company.name}
                description={company.code}
              />
            </List.Item>
          )}
        />
      </Card>
    </div>
  )
}

export default SelectCompanyPage
