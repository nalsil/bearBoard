import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Table, Button, Space, Tag, Popconfirm, message, Card } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table'
import dayjs from 'dayjs'
import { postsApi, boardsApi } from '../../api'
import type { Post, Board } from '../../types'

function PostListPage() {
  const [posts, setPosts] = useState<Post[]>([])
  const [board, setBoard] = useState<Board | null>(null)
  const [loading, setLoading] = useState(true)
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1,
    pageSize: 20,
    total: 0,
  })
  const navigate = useNavigate()
  const { boardId } = useParams()

  const fetchBoard = async () => {
    try {
      const response = await boardsApi.getById(Number(boardId))
      if (response.success && response.data) {
        setBoard(response.data)
      }
    } catch {
      message.error('게시판 정보를 불러오는데 실패했습니다.')
    }
  }

  const fetchPosts = async (page = 0, size = 20) => {
    setLoading(true)
    try {
      const response = await postsApi.getList(Number(boardId), page, size)
      if (response.success && response.data) {
        setPosts(response.data.content)
        setPagination({
          current: response.data.page + 1,
          pageSize: response.data.size,
          total: response.data.totalElements,
        })
      }
    } catch {
      message.error('게시글 목록을 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchBoard()
    fetchPosts()
  }, [boardId])

  const handleTableChange = (newPagination: TablePaginationConfig) => {
    fetchPosts((newPagination.current || 1) - 1, newPagination.pageSize)
  }

  const handleDelete = async (id: number) => {
    try {
      const response = await postsApi.delete(id)
      if (response.success) {
        message.success('게시글이 삭제되었습니다.')
        fetchPosts((pagination.current || 1) - 1, pagination.pageSize)
      } else {
        message.error(response.message || '삭제에 실패했습니다.')
      }
    } catch {
      message.error('삭제에 실패했습니다.')
    }
  }

  const columns: ColumnsType<Post> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '제목',
      dataIndex: 'title',
      key: 'title',
    },
    {
      title: '조회수',
      dataIndex: 'viewCount',
      key: 'viewCount',
      width: 100,
    },
    {
      title: '작성일',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 150,
      render: (date) => dayjs(date).format('YYYY-MM-DD'),
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
            onClick={() => navigate(`/boards/${boardId}/posts/${record.id}/edit`)}
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
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/boards')}>
            목록
          </Button>
          <h1 className="page-title">{board?.name || '게시글'} 관리</h1>
        </Space>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => navigate(`/boards/${boardId}/posts/new`)}
        >
          게시글 추가
        </Button>
      </div>

      <Card>
        <Table
          columns={columns}
          dataSource={posts}
          rowKey="id"
          loading={loading}
          pagination={pagination}
          onChange={handleTableChange}
        />
      </Card>
    </div>
  )
}

export default PostListPage
