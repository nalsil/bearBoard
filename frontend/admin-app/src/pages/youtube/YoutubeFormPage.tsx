import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Form, Input, InputNumber, Switch, Button, Card, Spin, message, Space } from 'antd'
import { youtubeApi } from '../../api'
import type { CreateYoutubeVideoRequest } from '../../types'

function YoutubeFormPage() {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const navigate = useNavigate()
  const { id } = useParams()
  const isEdit = !!id

  useEffect(() => {
    if (isEdit) {
      fetchVideo()
    }
  }, [id])

  const fetchVideo = async () => {
    setLoading(true)
    try {
      const response = await youtubeApi.getById(Number(id))
      if (response.success && response.data) {
        form.setFieldsValue(response.data)
      } else {
        message.error('유튜브 영상 정보를 불러오는데 실패했습니다.')
        navigate('/youtube')
      }
    } catch {
      message.error('유튜브 영상 정보를 불러오는데 실패했습니다.')
      navigate('/youtube')
    } finally {
      setLoading(false)
    }
  }

  const validateYoutubeUrl = (_: unknown, value: string) => {
    if (!value) {
      return Promise.reject(new Error('유튜브 URL을 입력해주세요'))
    }
    const youtubePattern = /^(https?:\/\/)?(www\.)?(youtube\.com\/watch\?v=|youtu\.be\/)[a-zA-Z0-9_-]+/
    if (!youtubePattern.test(value)) {
      return Promise.reject(new Error('올바른 유튜브 URL 형식이 아닙니다'))
    }
    return Promise.resolve()
  }

  const handleSubmit = async (values: CreateYoutubeVideoRequest) => {
    setSaving(true)
    try {
      const response = isEdit
        ? await youtubeApi.update(Number(id), values)
        : await youtubeApi.create(values)

      if (response.success) {
        message.success(isEdit ? '유튜브 영상이 수정되었습니다.' : '유튜브 영상이 생성되었습니다.')
        navigate('/youtube')
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
        <h1 className="page-title">{isEdit ? '유튜브 영상 수정' : '유튜브 영상 추가'}</h1>
      </div>

      <Card className="form-container">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{ displayOrder: 0, isHidden: false }}
        >
          <Form.Item
            name="title"
            label="제목"
            rules={[{ required: true, message: '제목을 입력해주세요' }]}
          >
            <Input placeholder="제목을 입력하세요" />
          </Form.Item>

          <Form.Item
            name="videoUrl"
            label="유튜브 URL"
            rules={[{ validator: validateYoutubeUrl }]}
            extra="예: https://www.youtube.com/watch?v=VIDEO_ID 또는 https://youtu.be/VIDEO_ID"
          >
            <Input placeholder="유튜브 URL을 입력하세요" />
          </Form.Item>

          <Form.Item name="description" label="설명">
            <Input.TextArea rows={4} placeholder="영상 설명을 입력하세요" />
          </Form.Item>

          <Form.Item name="displayOrder" label="표시 순서">
            <InputNumber style={{ width: '100%' }} min={0} />
          </Form.Item>

          <Form.Item name="isHidden" label="숨김 처리" valuePropName="checked">
            <Switch />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={saving}>
                {isEdit ? '수정' : '저장'}
              </Button>
              <Button onClick={() => navigate('/youtube')}>취소</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default YoutubeFormPage
