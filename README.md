# Expense Management

Ứng dụng Android quản lý thu nhập và chi tiêu cá nhân. App hỗ trợ ghi giao dịch nhanh, quản lý danh mục, thống kê trực quan, xuất/nhập Excel và trợ lý AI để hỏi nhanh về dữ liệu chi tiêu.

## Tải APK

Quét QR bên dưới để tải trực tiếp file APK:

| QR tải APK |
| --- |
| ![QR tải APK](images/qr_code.png) |

* **Link tải trực tiếp:** [Expense_Management.apk](https://github.com/kieenn005/Expense_Management/raw/main/release/Expense_Management.apk)
* **Link tải dự phòng (CDN):** [Expense_Management.apk (Direct Raw)](https://raw.githubusercontent.com/kieenn005/Expense_Management/main/release/Expense_Management.apk)

> [!NOTE]
> **Lưu ý cho Android:**
> - Nếu quét mã QR bằng ứng dụng quét QR mặc định hoặc ứng dụng nhắn tin (Zalo, Viber,...) mà không tải được file, hãy **sao chép liên kết** và dán vào trình duyệt web (như **Google Chrome** hoặc **Samsung Internet**) để tải về.
> - Sau khi tải xong, nếu hệ thống cảnh báo "Tệp có thể gây hại" hoặc "Nguồn không xác định", hãy chọn **Vẫn tải xuống** (Download anyway) hoặc **Cho phép cài đặt từ nguồn này** (Allow from this source) để tiến hành cài đặt.

## Tính năng chính

- Ghi thu nhập và chi tiêu theo ngày hoặc theo tháng.
- Nhập giao dịch bằng bàn phím số riêng, tự định dạng tiền theo dạng `8.000`, `80.000`, `800.000`.
- Sắp xếp giao dịch mới nhất lên đầu danh sách.
- Tìm kiếm giao dịch bằng nút kính lúp, không cần đăng ký VIP.
- Quản lý danh mục thu nhập và chi tiêu: thêm, sửa, xóa, đổi icon và màu danh mục.
- Danh mục mới luôn được đặt trước danh mục `Khác`.
- Thống kê theo danh mục, theo ngày và biểu đồ đường so sánh các ngày.
- Xuất dữ liệu ra Excel bằng tiếng Việt.
- Nhập dữ liệu từ file Excel đã xuất.
- Trợ lý AI dạng bong bóng nổi để hỏi nhanh và thêm giao dịch bằng câu tự nhiên.
- Xem lại lịch sử trò chuyện AI trong mục `Thêm`.
- Hỗ trợ tiếng Việt mặc định, có thể đổi sang tiếng Anh trong cài đặt.

## Màn hình trong app

- `Giao dịch`: xem tổng thu nhập, chi tiêu, số dư và danh sách giao dịch.
- `Thống kê`: xem biểu đồ theo danh mục, theo ngày và biểu đồ đường.
- Nút `+`: thêm giao dịch mới.
- `Cài đặt`: đổi ngôn ngữ, quản lý danh mục.
- `Thêm`: xuất Excel, nhập Excel, xóa dữ liệu và xem lịch sử trò chuyện AI.

## Dữ liệu người dùng

Dữ liệu được lưu cục bộ trên thiết bị bằng Realm Database. App không tự đồng bộ dữ liệu lên server.

Khi xuất Excel, file `Transactions.xlsx` được lưu vào thư mục Downloads của thiết bị. File xuất ra dùng tiếng Việt cho tiêu đề và dữ liệu như `Thu nhập`, `Chi tiêu`, `Tiền mặt`, `Ngân hàng`.

## Công nghệ sử dụng

- Java
- Android XML Layout
- Material Components
- Realm Database
- Apache POI cho nhập/xuất Excel
- ViewBinding
- Custom chart view cho thống kê

## Cách chạy dự án

1. Clone repository:

```bash
git clone https://github.com/kieenn005/Expense_Management.git
```

2. Mở thư mục dự án bằng Android Studio.

3. Chọn Gradle JDK là JBR của Android Studio nếu máy chưa cấu hình Java:

```text
File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK
```

4. Build APK debug bằng terminal:

```powershell
.\gradlew.bat :app:assembleDebug
```

5. File APK sau khi build nằm tại:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Cài APK vào máy ảo

Khi emulator đang chạy, có thể cài APK bằng lệnh:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Nếu emulator báo `offline`, hãy restart lại emulator hoặc chạy lại ADB server.

## Ghi chú

APK public để tải QR đang được đặt tại:

```text
release/Expense_Management.apk
```

Sau khi build bản mới, hãy thay file này bằng APK mới rồi cập nhật/push repository để link QR tải đúng phiên bản mới nhất.

