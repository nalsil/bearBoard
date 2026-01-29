import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Form, Input, Switch, Button, Card, Spin, message, Space } from 'antd'
import { postsApi } from '../../api'
import type { CreatePostRequest } from '../../types'

function PostFormPage() {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const navigate = useNavigate()
  const { boardId, id } = useParams()
  const isEdit = !!id

  useEffect(() => {
    if (isEdit) {
      fetchPost()
    }
  }, [id])

  const fetchPost = async () => {
    setLoading(true)
    try {
      const response = await postsApi.getById(Number(id))
      if (response.success && response.data) {
        form.setFieldsValue(response.data)
      } else {
        message.error('게시글 정보를 불러오는데 실패했습니다.')
        navigate(`/boards/${boardId}/posts`)
      }
    } catch {
      message.error('게시글 정보를 불러오는데 실패했습니다.')
      navigate(`/boards/${boardId}/posts`)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (values: Omit<CreatePostRequest, 'boardId'>) => {
    setSaving(true)
    try {
      const data: CreatePostRequest = {
        ...values,
        boardId: Number(boardId),
      }

      const response = isEdit
        ? await postsApi.update(Number(id), data)
        : await postsApi.create(data)

      if (response.success) {
        message.success(isEdit ? '게시글이 수정되었습니다.' : '게시글이 생성되었습니다.')
        navigate(`/boards/${boardId}/posts`)
      } else {
        message.error(response.message || '저장에 실패했습니다.')
      }
    } catch {
      message.error('저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 50 }}>
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">{isEdit ? '게시글 수정' : '게시글 추가'}</h1>
      </div>

      <Card className="form-container">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{ isHidden: false }}
        >
          <Form.Item
            name="title"
            label="제목"
            rules={[{ required: true, message: '제목을 입력해주세요' }]}
          >
            <Input placeholder="제목을 입력하세요" />
          </Form.Item>

          <Form.Item
            name="content"
            label="내용"
            rules={[{ required: true, message: '내용을 입력해주세요' }]}
          >
            <Input.TextArea rows={10} placeholder="내용을 입력하세요" />
          </Form.Item>

          <Form.Item name="filePath" label="첨부파일 경로">
            <Input placeholder="첨부파일 경로를 입력하세요" />
          </Form.Item>

          <Form.Item name="isHidden" label="숨김 처리" valuePropName="checked">
            <Switch />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={saving}>
                {isEdit ? '수정' : '저장'}
              </Button>
              <Button onClick={() => navigate(`/boards/${boardId}/posts`)}>취소</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default PostFormPage
