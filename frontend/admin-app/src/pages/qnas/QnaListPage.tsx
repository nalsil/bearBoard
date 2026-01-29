import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Table, Button, Space, Tag, Popconfirm, message, Card, Radio } from 'antd'
import { EyeOutlined, DeleteOutlined, EyeInvisibleOutlined } from '@ant-design/icons'
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table'
import type { RadioChangeEvent } from 'antd'
import dayjs from 'dayjs'
import { qnasApi } from '../../api'
import type { Qna } from '../../types'

type FilterStatus = 'all' | 'unanswered' | 'answered'

function QnaListPage() {
  const [qnas, setQnas] = useState<Qna[]>([])
  const [loading, setLoading] = useState(true)
  const [filterStatus, setFilterStatus] = useState<FilterStatus>('all')
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1,
    pageSize: 20,
    total: 0,
  })
  const navigate = useNavigate()

  const fetchQnas = async (page = 0, size = 20, status: FilterStatus = filterStatus) => {
    setLoading(true)
    try {
      const answeredFilter = status === 'unanswered' ? 'false' : status === 'answered' ? 'true' : undefined
      const response = await qnasApi.getList(page, size, answeredFilter)
      if (response.success && response.data) {
        setQnas(response.data.content)
        setPagination({
          current: response.data.page + 1,
          pageSize: response.data.size,
          total: response.data.totalElements,
        })
      }
    } catch {
      message.error('Q&A 목록을 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchQnas()
  }, [])

  const handleTableChange = (newPagination: TablePaginationConfig) => {
    fetchQnas((newPagination.current || 1) - 1, newPagination.pageSize, filterStatus)
  }

  const handleFilterChange = (e: RadioChangeEvent) => {
    const status = e.target.value as FilterStatus
    setFilterStatus(status)
    fetchQnas(0, pagination.pageSize, status)
  }

  const handleHide = async (id: number, isHidden: boolean) => {
    try {
      const response = await qnasApi.hide(id, !isHidden)
      if (response.success) {
        message.success(isHidden ? 'Q&A가 공개되었습니다.' : 'Q&A가 숨김 처리되었습니다.')
        fetchQnas((pagination.current || 1) - 1, pagination.pageSize, filterStatus)
      } else {
        message.error(response.message || '처리에 실패했습니다.')
      }
    } catch {
      message.error('처리에 실패했습니다.')
    }
  }

  const handleDelete = async (id: number) => {
    try {
      const response = await qnasApi.delete(id)
      if (response.success) {
        message.success('Q&A가 삭제되었습니다.')
        fetchQnas((pagination.current || 1) - 1, pagination.pageSize, filterStatus)
      } else {
        message.error(response.message || '삭제에 실패했습니다.')
      }
    } catch {
      message.error('삭제에 실패했습니다.')
    }
  }

  const columns: ColumnsType<Qna> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '이메일',
      dataIndex: 'askerEmail',
      key: 'askerEmail',
      width: 180,
      ellipsis: true,
    },
    {
      title: '질문',
      dataIndex: 'questionTitle',
      key: 'questionTitle',
      ellipsis: true,
    },
    {
      title: '작성일',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 120,
      render: (date) => dayjs(date).format('YYYY-MM-DD'),
    },
    {
      title: '답변상태',
      key: 'answerStatus',
      width: 100,
      render: (_, record) => (
        <Tag color={record.isAnswered ? 'green' : 'orange'}>
          {record.isAnswered ? '답변완료' : '대기중'}
        </Tag>
      ),
    },
    {
      title: '상태',
      dataIndex: 'isHidden',
      key: 'isHidden',
      width: 80,
      render: (isHidden) => (
        <Tag color={isHidden ? 'default' : 'green'}>
          {isHidden ? '숨김' : '공개'}
        </Tag>
      ),
    },
    {
      title: '작업',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/qnas/${record.id}/answer`)}
          >
            {record.isAnswered ? '보기' : '답변'}
          </Button>
          <Button
            type="link"
            icon={record.isHidden ? <EyeOutlined /> : <EyeInvisibleOutlined />}
            onClick={() => handleHide(record.id, record.isHidden)}
          >
            {record.isHidden ? '공개' : '숨김'}
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
        <h1 className="page-title">Q&A 관리</h1>
      </div>

      <Card>
        <div style={{ marginBottom: 16 }}>
          <Radio.Group value={filterStatus} onChange={handleFilterChange}>
            <Radio.Button value="all">전체</Radio.Button>
            <Radio.Button value="unanswered">미답변</Radio.Button>
            <Radio.Button value="answered">답변완료</Radio.Button>
          </Radio.Group>
        </div>

        <Table
          columns={columns}
          dataSource={qnas}
          rowKey="id"
          loading={loading}
          pagination={pagination}
          onChange={handleTableChange}
        />
      </Card>
    </div>
  )
}

export default QnaListPage
