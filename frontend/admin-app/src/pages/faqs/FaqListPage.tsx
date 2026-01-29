import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Table, Button, Space, Tag, Popconfirm, message, Card } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table'
import { faqsApi } from '../../api'
import type { Faq } from '../../types'

function FaqListPage() {
  const [faqs, setFaqs] = useState<Faq[]>([])
  const [loading, setLoading] = useState(true)
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1,
    pageSize: 20,
    total: 0,
  })
  const navigate = useNavigate()

  const fetchFaqs = async (page = 0, size = 20) => {
    setLoading(true)
    try {
      const response = await faqsApi.getList(page, size)
      if (response.success && response.data) {
        setFaqs(response.data.content)
        setPagination({
          current: response.data.page + 1,
          pageSize: response.data.size,
          total: response.data.totalElements,
        })
      }
    } catch {
      message.error('FAQ 목록을 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchFaqs()
  }, [])

  const handleTableChange = (newPagination: TablePaginationConfig) => {
    fetchFaqs((newPagination.current || 1) - 1, newPagination.pageSize)
  }

  const handleDelete = async (id: number) => {
    try {
      const response = await faqsApi.delete(id)
      if (response.success) {
        message.success('FAQ가 삭제되었습니다.')
        fetchFaqs((pagination.current || 1) - 1, pagination.pageSize)
      } else {
        message.error(response.message || '삭제에 실패했습니다.')
      }
    } catch {
      message.error('삭제에 실패했습니다.')
    }
  }

  const columns: ColumnsType<Faq> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '카테고리',
      dataIndex: 'category',
      key: 'category',
      width: 150,
      render: (category) => category || '-',
    },
    {
      title: '질문',
      dataIndex: 'question',
      key: 'question',
      ellipsis: true,
    },
    {
      title: '표시순서',
      dataIndex: 'displayOrder',
      key: 'displayOrder',
      width: 100,
    },
    {
      title: '상태',
      dataIndex: 'isHidden',
      key: 'isHidden',
      width: 100,
      render: (isHidden) => (
        <Tag color={isHidden ? 'default' : 'green'}>
          {isHidden ? '숨김' : '공개'}
        </Tag>
      ),
    },
    {
      title: '작업',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => navigate(`/faqs/${record.id}/edit`)}
          >
            수정
          </Button>
          <Popconfirm
            title="정말 삭제하시겠습니까?"
            onConfirm={() => handleDelete(record.id)}
            okText="삭제"
            cancelText="취소"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              삭제
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">FAQ 관리</h1>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => navigate('/faqs/new')}
        >
          FAQ 추가
        </Button>
      </div>

      <Card>
        <Table
          columns={columns}
          dataSource={faqs}
          rowKey="id"
          loading={loading}
          pagination={pagination}
          onChange={handleTableChange}
        />
      </Card>
    </div>
  )
}

export default FaqListPage
