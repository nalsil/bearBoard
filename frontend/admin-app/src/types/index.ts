// API Response types
export interface ApiResponse<T> {
  success: boolean
  message: string
  data?: T
  errorCode?: string
  timestamp: string
}

export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  numberOfElements: number
  empty: boolean
}

// Auth types
export interface AuthResponse {
  adminId: number
  username: string
  name: string
  email: string
  role: string
  companyId: number | null
  companyCode: string | null
  companyName: string | null
}

export interface LoginRequest {
  username: string
  password: string
}

// Dashboard types
export interface DashboardStats {
  productCount: number
  postCount: number
  faqCount: number
  qnaCount: number
  pendingQnaCount: number
  youtubeCount: number
  company: CompanyInfo | null
}

export interface CompanyInfo {
  id: number
  code: string
  name: string
  logoUrl: string | null
}

// Product types
export interface Product {
  id: number
  companyId: number
  name: string
  category: string | null
  description: string | null
  price: number | null
  imageUrl: string | null
  displayOrder: number
  isHidden: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateProductRequest {
  name: string
  category?: string
  description?: string
  price?: number
  imageUrl?: string
  displayOrder?: number
  isHidden?: boolean
}

// Board types
export interface Board {
  id: number
  companyId: number
  name: string
  type: string
  createdAt: string
}

// Post types
export interface Post {
  id: number
  boardId: number
  title: string
  content: string
  viewCount: number
  isHidden: boolean
  filePath: string | null
  createdAt: string
  updatedAt: string
}

export interface CreatePostRequest {
  boardId: number
  title: string
  content: string
  isHidden?: boolean
  filePath?: string
}

// FAQ types
export interface Faq {
  id: number
  companyId: number
  category: string | null
  question: string
  answer: string
  displayOrder: number
  isHidden: boolean
  createdAt: string
}

export interface CreateFaqRequest {
  category?: string
  question: string
  answer: string
  displayOrder?: number
  isHidden?: boolean
}

// QnA types
export interface Qna {
  id: number
  companyId: number
  questionTitle: string
  questionBody: string
  askerEmail: string
  answerBody: string | null
  answererId: number | null
  isAnswered: boolean
  isHidden: boolean
  createdAt: string
  answeredAt: string | null
}

export interface AnswerQnaRequest {
  answerBody: string
}

// YouTube types
export interface YoutubeVideo {
  id: number
  companyId: number
  videoUrl: string
  title: string
  description: string | null
  thumbnailUrl: string | null
  displayOrder: number
  isHidden: boolean
  createdAt: string
}

export interface CreateYoutubeVideoRequest {
  videoUrl: string
  title: string
  description?: string
  thumbnailUrl?: string
  displayOrder?: number
  isHidden?: boolean
}

// Company types
export interface Company {
  id: number
  code: string
  name: string
  logoUrl: string | null
  primaryColor: string | null
  secondaryColor: string | null
  description: string | null
  address: string | null
  phone: string | null
  email: string | null
  latitude: number | null
  longitude: number | null
  foundedDate: string | null
  isActive: boolean
  createdAt: string
  updatedAt: string
}
