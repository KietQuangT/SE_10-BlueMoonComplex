import type {
  AccountRecord,
  CollectionRateSummary,
  FeeTypeRate,
  FieldChangeProposal,
  Household,
  InvoiceRecord,
  PaymentTransaction,
  ReportPackage,
  ResidencyChangeRecord,
  ResidentMember,
  SessionUser,
  VoluntaryCampaign,
} from '../types/domain';

export const DEMO_SESSIONS: Record<string, SessionUser> = {
  admin: {
    username: 'demo-admin',
    fullName: 'Demo Ban Quản Trị',
    role: 'ADMIN',
    householdId: null,
    mustChangePassword: false,
  },
  accountant: {
    username: 'demo-ketoan',
    fullName: 'Demo Kế Toán',
    role: 'ACCOUNTANT',
    householdId: null,
    mustChangePassword: false,
  },
  resident: {
    username: 'demo-cudan',
    fullName: 'Cư Dân Mẫu A',
    role: 'RESIDENT',
    householdId: 1,
    mustChangePassword: false,
  },
  residentFirstLogin: {
    username: 'demo-cudan-moi',
    fullName: 'Cư Dân Mẫu D',
    role: 'RESIDENT',
    householdId: 2,
    mustChangePassword: true,
  },
  authority: {
    username: 'demo-coquan',
    fullName: 'Cán Bộ Thanh Tra Phường',
    role: 'AUTHORITY',
    householdId: null,
    mustChangePassword: false,
  },
};

export const INITIAL_HOUSEHOLDS: Household[] = [
  {
    id: 1,
    householdCode: 'H-0001',
    apartmentCode: 'A01',
    areaM2: '50.00',
    address: 'Tòa A, tầng 1',
    status: 'ACTIVE',
    currentResidents: 3,
  },
  {
    id: 2,
    householdCode: 'H-0002',
    apartmentCode: 'A02',
    areaM2: '72.50',
    address: 'Tòa A, tầng 1',
    status: 'ACTIVE',
    currentResidents: 1,
  },
  {
    id: 3,
    householdCode: 'H-0003',
    apartmentCode: 'B05',
    areaM2: '64.00',
    address: 'Tòa B, tầng 5',
    status: 'INACTIVE',
    currentResidents: 0,
  },
];

export const INITIAL_RESIDENTS: ResidentMember[] = [
  {
    id: 1,
    householdId: 1,
    fullName: 'Cư Dân Mẫu A',
    identityNumberMasked: '********1234',
    dateOfBirth: '1985-03-12',
    relationshipToHead: 'Chủ hộ',
    residencyStatus: 'Đang cư trú',
  },
  {
    id: 2,
    householdId: 1,
    fullName: 'Cư Dân Mẫu B',
    identityNumberMasked: '********5678',
    dateOfBirth: '1988-07-30',
    relationshipToHead: 'Vợ/Chồng',
    residencyStatus: 'Tạm vắng',
  },
  {
    id: 3,
    householdId: 1,
    fullName: 'Cư Dân Mẫu C',
    identityNumberMasked: null,
    dateOfBirth: '2015-01-05',
    relationshipToHead: 'Con',
    residencyStatus: 'Đang cư trú',
  },
  {
    id: 4,
    householdId: 2,
    fullName: 'Cư Dân Mẫu D',
    identityNumberMasked: '********9012',
    dateOfBirth: '1970-11-21',
    relationshipToHead: 'Chủ hộ',
    residencyStatus: 'Đang cư trú',
  },
  {
    id: 5,
    householdId: 2,
    fullName: 'Cư Dân Mẫu E',
    identityNumberMasked: '********3456',
    dateOfBirth: '1972-02-02',
    relationshipToHead: 'Vợ/Chồng',
    residencyStatus: 'Đã chuyển đi',
  },
];

export const INITIAL_ACCOUNTS: AccountRecord[] = [
  {
    id: 1,
    fullName: 'Demo Ban Quản Trị',
    username: 'demo-admin',
    email: 'admin@example.test',
    role: 'ADMIN',
    householdCode: null,
    householdId: null,
    status: 'ACTIVE',
    locked: false,
  },
  {
    id: 2,
    fullName: 'Demo Kế Toán',
    username: 'demo-ketoan',
    email: 'ketoan@example.test',
    role: 'ACCOUNTANT',
    householdCode: null,
    householdId: null,
    status: 'ACTIVE',
    locked: true,
    lockedForMinutes: 45,
  },
  {
    id: 3,
    fullName: 'Cư Dân Mẫu A',
    username: 'demo-cudan',
    email: 'cudan@example.test',
    role: 'RESIDENT',
    householdCode: 'H-0001',
    householdId: 1,
    status: 'ACTIVE',
    locked: false,
  },
];

export const INITIAL_RESIDENCY_CHANGES: ResidencyChangeRecord[] = [
  {
    id: 1,
    residentId: 2,
    householdId: 1,
    changeType: 'TAM_VANG',
    effectiveDate: '2026-11-10',
    endDateAssumed: '2026-11-20',
    reason: 'Đi công tác',
    status: 'PENDING',
    recordedBy: 3,
  },
  {
    id: 2,
    residentId: 5,
    householdId: 2,
    changeType: 'CHUYEN_DI',
    effectiveDate: '2026-10-01',
    endDateAssumed: null,
    reason: 'Chuyển nơi ở',
    status: 'APPROVED',
    recordedBy: 1,
    approvedByAssumed: 1,
    approvedAtAssumed: '2026-10-02T03:15:00Z',
  },
  {
    id: 3,
    residentId: 3,
    householdId: 1,
    changeType: 'TAM_VANG',
    effectiveDate: '2026-09-01',
    endDateAssumed: '2026-09-05',
    reason: 'Về quê',
    status: 'REJECTED',
    recordedBy: 3,
    rejectReasonAssumed: 'Thiếu thông tin ngày kết thúc',
  },
];

export const INITIAL_FEE_RATES: FeeTypeRate[] = [
  {
    id: 1,
    code: 'QUAN_LY',
    name: 'Phí quản lý',
    unit: 'm2',
    unitPrice: '10000',
    effectiveFrom: '2026-01-01',
  },
  {
    id: 2,
    code: 'RAC',
    name: 'Phí rác',
    unit: 'hộ',
    unitPrice: '60000',
    effectiveFrom: '2026-01-01',
  },
];

export const INITIAL_INVOICES: InvoiceRecord[] = [
  {
    id: 1,
    householdId: 1,
    apartmentCode: 'A01',
    period: '2026-10',
    issuedAt: '2026-10-31T01:00:00Z',
    dueDate: '2026-11-15',
    status: 'CHUA_DONG',
    lines: [
      {
        feeCode: 'QUAN_LY',
        feeName: 'Phí quản lý',
        quantity: '50.00',
        unit: 'm2',
        unitPrice: '10000',
        amount: '500000',
      },
      {
        feeCode: 'RAC',
        feeName: 'Phí rác',
        quantity: '1',
        unit: 'hộ',
        unitPrice: '60000',
        amount: '60000',
      },
    ],
    total: '560000',
    paid: '0',
    remaining: '560000',
  },
  {
    id: 2,
    householdId: 1,
    apartmentCode: 'A01',
    period: '2026-09',
    issuedAt: '2026-09-30T01:00:00Z',
    dueDate: '2026-10-15',
    status: 'DA_DONG',
    lines: [
      {
        feeCode: 'QUAN_LY',
        feeName: 'Phí quản lý',
        quantity: '50.00',
        unit: 'm2',
        unitPrice: '10000',
        amount: '500000',
      },
      {
        feeCode: 'RAC',
        feeName: 'Phí rác',
        quantity: '1',
        unit: 'hộ',
        unitPrice: '60000',
        amount: '60000',
      },
    ],
    total: '560000',
    paid: '560000',
    remaining: '0',
  },
  {
    id: 3,
    householdId: 2,
    apartmentCode: 'A02',
    period: '2026-10',
    issuedAt: '2026-10-31T01:00:00Z',
    dueDate: '2026-11-15',
    status: 'CHUA_DONG',
    lines: [
      {
        feeCode: 'QUAN_LY',
        feeName: 'Phí quản lý',
        quantity: '72.50',
        unit: 'm2',
        unitPrice: '10000',
        amount: '725000',
      },
      {
        feeCode: 'RAC',
        feeName: 'Phí rác',
        quantity: '1',
        unit: 'hộ',
        unitPrice: '60000',
        amount: '60000',
      },
    ],
    total: '785000',
    paid: '0',
    remaining: '785000',
  },
];

export const INITIAL_PAYMENTS: PaymentTransaction[] = [
  {
    id: 1,
    invoiceId: 2,
    apartmentCode: 'A01',
    period: '2026-09',
    amount: '560000',
    paidAt: '2026-10-10T02:30:00Z',
    receivedBy: 'Demo Kế Toán',
  },
];

export const INITIAL_VOLUNTARY_CAMPAIGNS: VoluntaryCampaign[] = [
  {
    id: 1,
    name: 'Quỹ hỗ trợ cây xanh',
    description: 'Bổ sung cây bóng mát và bồn hoa sảnh tầng đế Tòa A – B.',
    status: 'PUBLISHED',
    opensOn: '2026-11-01',
    closesOn: '2026-11-30',
    note: 'Tự nguyện, không tính vào công nợ',
    contributionsCount: 14,
    totalCollectedDisplay: '4.200.000',
  },
];

export const INITIAL_REPORT_PACKAGES: ReportPackage[] = [
  {
    id: 1,
    type: 'DANH_SACH_LUU_TRU',
    typeLabel: 'Danh sách lưu trú',
    period: '2026-Q4',
    status: 'APPROVED',
    approvedBy: 'Demo Ban Quản Trị',
    approvedAt: '2026-11-05T07:00:00Z',
    allowedFields: ['fullName', 'apartmentCode', 'residencyStatus'],
  },
];

export const INITIAL_COLLECTION_RATE: CollectionRateSummary = {
  period: '2026-10',
  totalCollected: '12500000',
  totalOutstanding: '3500000',
  rate: '78,1 %',
  formulaNote: 'Công thức mẫu số chờ chốt (Q10)',
};

export const FIELD_CHANGE_PROPOSALS: FieldChangeProposal[] = [
  {
    id: 1,
    screenCode: 'M-16 / M-31',
    fieldOrScreen: 'end_date (Ngày kết thúc tạm vắng)',
    reason: 'PBI-007/008 cần hiển thị khoảng thời gian tạm vắng trên đơn cư dân và màn duyệt BQT',
    required: true,
    ownerQuestion: 'Q2 — Sơn, Tuấn',
  },
  {
    id: 2,
    screenCode: 'M-16 / M-31',
    fieldOrScreen: 'reject_reason (Lý do từ chối đơn tạm vắng)',
    reason: 'Cư dân cần biết lý do bị từ chối ở M-31 để khai báo bổ sung đúng quy định',
    required: true,
    ownerQuestion: 'Q2 — Sơn, Tuấn',
  },
  {
    id: 3,
    screenCode: 'M-10',
    fieldOrScreen: 'locked (Cờ boolean đang khóa tạm thời do server tính)',
    reason: 'users.locked_until không tự về NULL khi hết khóa; UI không tự so giờ máy trạm',
    required: true,
    ownerQuestion: 'Q25 — Sơn, Tuấn',
  },
  {
    id: 4,
    screenCode: 'M-21',
    fieldOrScreen: 'Quyền tra cứu danh sách mã căn hộ cho Kế toán',
    reason: 'Ma trận F01 đang hoãn quyền Kế toán xem hộ nhưng M-21 bắt buộc tìm căn để thu tiền mặt',
    required: true,
    ownerQuestion: 'Q20 — Sơn, Quân',
  },
  {
    id: 5,
    screenCode: 'M-13',
    fieldOrScreen: 'Danh sách tài khoản Cư dân gắn với hộ (chỉ đọc cho BQT)',
    reason: 'Hỗ trợ PBI-001/004 khi BQT kiểm tra hộ đã có account hay cần vô hiệu hóa',
    required: false,
    ownerQuestion: 'Q23 — Sơn, Tuấn',
  },
  {
    id: 6,
    screenCode: 'M-32 / M-33',
    fieldOrScreen: 'due_date (Hạn nộp hóa đơn) & Lịch sử giao dịch đóng phí',
    reason: 'WBS 2.4.1.2 và trang đầu cư dân cần hiển thị hạn nộp và biên nhận đã thanh toán',
    required: true,
    ownerQuestion: 'Q9 / S08 / S10 — Quân, Kiệt',
  },
];

export const STORYBOARDS = [
  {
    code: 'L1',
    title: 'Đăng nhập -> vào trang đầu theo vai trò (tối đa 2 thao tác)',
    role: 'Mọi vai trò',
    steps: [
      { label: '1. Mở M-01, nhập Tên đăng nhập & Mật khẩu', path: '/login' },
      { label: '2. Nhấn Đăng nhập -> gọi /auth/me dựng menu', path: '/resident/invoices' },
      { label: 'Nhánh lỗi: 401 hiện thông điệp chung duy nhất', path: '/login' },
    ],
  },
  {
    code: 'L3',
    title: 'Cư dân khai báo tạm vắng -> BQT duyệt -> Cư dân xem kết quả (3 thao tác/vai trò)',
    role: 'Cư dân & BQT',
    steps: [
      { label: '1. Cư dân mở M-31, chọn thành viên + ngày + lý do rồi Gửi', path: '/resident/absence' },
      { label: '2. BQT mở M-16, xem đơn chờ duyệt -> Duyệt đơn -> Xác nhận', path: '/admin/absence-approvals' },
      { label: '3. Cư dân kiểm tra mục Đơn của tôi ở M-31 (Đã duyệt / Từ chối kèm lý do)', path: '/resident/absence' },
    ],
  },
  {
    code: 'L5',
    title: 'Cư dân kiểm tra công nợ & xem chi tiết hóa đơn (1–2 thao tác)',
    role: 'Cư dân',
    steps: [
      { label: '1. Vào trang đầu M-32 thấy ngay dải Còn nợ · Hạn nộp · Đơn đang chờ', path: '/resident/invoices' },
      { label: '2. Bấm vào kỳ hóa đơn 10/2026 -> mở M-33 xem thành phần snapshot', path: '/resident/invoices/1' },
      { label: 'Nhánh lỗi: truy cập chéo hộ hoặc URL BQT -> M-04 (403)', path: '/error/403' },
    ],
  },
];
