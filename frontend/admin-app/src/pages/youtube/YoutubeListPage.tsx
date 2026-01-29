import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Table, Button, Space, Tag, Popconfirm, message, Card, Image } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, PlayCircleOutlined } from '@ant-design/icons'
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table'
import { youtubeApi } from '../../api'
import type { YoutubeVideo } from '../../types'

function YoutubeListPage() {
  const [videos, setVideos] = useState<YoutubeVideo[]>([])
  const [loading, setLoading] = useState(true)
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1,
    pageSize: 20,
    total: 0,
  })
  const navigate = useNavigate()

  const fetchVideos = async (page = 0, size = 20) => {
    setLoading(true)
    try {
      const response = await youtubeApi.getList(page, size)
      if (response.success && response.data) {
        setVideos(response.data.content)
        setPagination({
          current: response.data.page + 1,
          pageSize: response.data.size,
          total: response.data.totalElements,
        })
      }
    } catch {
      message.error('유튜브 영상 목록을 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchVideos()
  }, [])

  const handleTableChange = (newPagination: TablePaginationConfig) => {
    fetchVideos((newPagination.current || 1) - 1, newPagination.pageSize)
  }

  const handleDelete = async (id: number) => {
    try {
      const response = await youtubeApi.delete(id)
      if (response.success) {
        message.success('유튜브 영상이 삭제되었습니다.')
        fetchVideos((pagination.current || 1) - 1, pagination.pageSize)
      } else {
        message.error(response.message || '삭제에 실패했습니다.')
      }
    } catch {
      message.error('삭제에 실패했습니다.')
    }
  }

  const getYoutubeThumbnail = (videoUrl: string): string => {
    const videoIdMatch = videoUrl.match(/(?:youtube\.com\/watch\?v=|youtu\.be\/)([^&\s]+)/)
    if (videoIdMatch && videoIdMatch[1]) {
      return `https://img.youtube.com/vi/${videoIdMatch[1]}/mqdefault.jpg`
    }
    return ''
  }

  const columns: ColumnsType<YoutubeVideo> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '썸네일',
      key: 'thumbnail',
      width: 180,
      render: (_, record) => {
        const thumbnail = getYoutubeThumbnail(record.videoUrl)
        return thumbnail ? (
          <Image
            src={thumbnail}
            alt={record.title}
            width={160}
            height={90}
            style={{ objectFit: 'cover', borderRadius: 4 }}
            fallback="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMIAAADDCAYAAADQvc6UAAABRWlDQ1BJQ0MgUHJvZmlsZQAAKJFjYGASSSwoyGFhYGDIzSspCnJ3UoiIjFJgf8LAwSDCIMogwMCcmFxc4BgQ4ANUwgCjUcG3awyMIPqyLsis7PPOq3QdDFcvjV3jOD1boQVTPQrgSkktTgbSf4A4LbmgqISBgTEFyFYuLykAsTuAbJEioKOA7DkgdjqEvQHEToKwj4DVhAQ5A9k3gGyB5IxEoBmML4BsnSQk8XQkNtReEOBxcfXxUQg1Mjc0dyHgXNJBSWpFCYh2zi+oLMpMzyhRcASGUqqCZ16yno6CkYGRAQMDKMwhqj/fAIcloxgHQqxAjIHBEugw5sUIsSQpBobtQPdLciLEVJYzMPBHMDBsayhILEqEO4DxG0txmrERhM29nYGBddr//5/DGRjYNRkY/l7////39v///y4Dmn+LgeHANwDrkl1AuO+pmgAAADhlWElmTU0AKgAAAAgAAYdpAAQAAAABAAAAGgAAAAAAAqACAAQAAAABAAAAwqADAAQAAAABAAAAwwAAAAD9b/HnAAAHlklEQVR4Ae3dP3PTWBSGcbGzM6GCKqlIBRV0dHRJFarQ0eUT8LH4BnRU0NHR0UEFVdIlFRV7TzRksomPY8uykTk/zewQfKw/9444l"
          />
        ) : (
          <div
            style={{
              width: 160,
              height: 90,
              backgroundColor: '#f0f0f0',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              borderRadius: 4,
            }}
          >
            <PlayCircleOutlined style={{ fontSize: 32, color: '#999' }} />
          </div>
        )
      },
    },
    {
      title: '제목',
      dataIndex: 'title',
      key: 'title',
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
      width: 180,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<PlayCircleOutlined />}
            onClick={() => window.open(record.videoUrl, '_blank')}
          >
            보기
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => navigate(`/youtube/${record.id}/edit`)}
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
        <h1 className="page-title">유튜브 관리</h1>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => navigate('/youtube/new')}
        >
          영상 추가
        </Button>
      </div>

      <Card>
        <Table
          columns={columns}
          dataSource={videos}
          rowKey="id"
          loading={loading}
          pagination={pagination}
          onChange={handleTableChange}
        />
      </Card>
    </div>
  )
}

export default YoutubeListPage
