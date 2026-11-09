/** Human labels shared by report configuration and preview. Wire keys stay unchanged. */
const FIELD_LABELS: Record<string, string> = {
  apartmentCode: 'Căn hộ', fullName: 'Họ và tên', residencyStatus: 'Trạng thái cư trú',
  dateOfBirth: 'Ngày sinh', relationshipToHead: 'Quan hệ với chủ hộ',
  identityNumber: 'Số giấy tờ định danh', phone: 'Số điện thoại',
};
export const reportFieldLabel = (key: string) => FIELD_LABELS[key] ?? key;
