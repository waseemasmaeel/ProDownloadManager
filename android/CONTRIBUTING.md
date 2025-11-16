# المساهمة في Pro Download Manager

شكراً لاهتمامك بالمساهمة في **Pro Download Manager**! 🎉

---

## 📋 **جدول المحتويات**

1. [كيفية المساهمة](#كيفية-المساهمة)
2. [معايير الكود](#معايير-الكود)
3. [عملية التطوير](#عملية-التطوير)
4. [الإبلاغ عن الأخطاء](#الإبلاغ-عن-الأخطاء)
5. [اقتراح ميزات](#اقتراح-ميزات)
6. [مراجعة الكود](#مراجعة-الكود)

---

## 🤝 **كيفية المساهمة**

### **1. Fork المشروع**
```bash
# اضغط على زر Fork في GitHub
```

### **2. Clone مشروعك**
```bash
git clone https://github.com/YOUR_USERNAME/ProDownloadManager.git
cd ProDownloadManager
```

### **3. أنشئ Branch جديد**
```bash
git checkout -b feature/amazing-feature
```

### **4. قم بالتعديلات**
```bash
# اعمل على الكود
# اختبر التغييرات
```

### **5. Commit التغييرات**
```bash
git add .
git commit -m "Add: وصف واضح للتغييرات"
```

### **6. Push إلى GitHub**
```bash
git push origin feature/amazing-feature
```

### **7. افتح Pull Request**
```
اذهب إلى GitHub وافتح PR من branch-ك
```

---

## 📝 **معايير الكود**

### **Kotlin Style Guide**

```kotlin
// ✅ استخدم أسماء واضحة
fun downloadFile(url: String, fileName: String) { }

// ✅ استخدم Coroutines
suspend fun startDownload() = withContext(Dispatchers.IO) { }

// ✅ استخدم data class للبيانات
data class DownloadItem(val id: String, val url: String)

// ✅ استخدم sealed class للحالات
sealed class DownloadState {
    object Loading : DownloadState()
    data class Success(val data: Data) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

// ✅ توثيق الدوال المعقدة
/**
 * Downloads file using multi-part download
 * @param url File URL
 * @param threads Number of threads (1-16)
 * @return Download ID
 */
suspend fun downloadWithThreads(url: String, threads: Int): String
```

### **التنسيق:**
```kotlin
// ✅ المسافات
class MyClass {  // مسافة قبل {
    fun myFunction() {
        if (condition) {
            doSomething()
        }
    }
}

// ✅ الأسطر الطويلة
val result = downloadManager.addDownload(
    url = url,
    fileName = fileName,
    filePath = path
)
```

---

## 🔧 **عملية التطوير**

### **متطلبات التطوير:**
```
✅ Android Studio Hedgehog
✅ JDK 17
✅ Git
✅ معرفة بـ Kotlin
✅ معرفة بـ Jetpack Compose
```

### **إعداد البيئة:**

```bash
# 1. Clone المشروع
git clone https://github.com/YOUR_USERNAME/ProDownloadManager.git

# 2. افتح في Android Studio
# File > Open > ProDownloadManager

# 3. Sync Gradle
# ينزل جميع التبعيات تلقائياً

# 4. اختبر البناء
./gradlew build
```

---

## 🐛 **الإبلاغ عن الأخطاء**

### **قبل الإبلاغ:**
1. ✅ ابحث في Issues الموجودة
2. ✅ تأكد أنك تستخدم آخر إصدار
3. ✅ جرب إعادة تشغيل التطبيق

### **معلومات مطلوبة:**

```markdown
## وصف المشكلة
وصف واضح وموجز للمشكلة

## خطوات إعادة المشكلة
1. اذهب إلى '...'
2. اضغط على '....'
3. شاهد الخطأ

## السلوك المتوقع
ما كان يجب أن يحدث

## السلوك الفعلي
ما حدث بالفعل

## Screenshots
إن أمكن، أرفق صور

## البيئة
- إصدار التطبيق: [مثال: 1.0.0]
- Android: [مثال: Android 13]
- الجهاز: [مثال: Samsung Galaxy S21]

## Logs
```
أرفق سجلات logcat
```
```

### **Priority Labels:**
```
🔴 Critical: يوقف التطبيق
🟠 High: ميزة رئيسية لا تعمل
🟡 Medium: ميزة ثانوية لا تعمل
🟢 Low: تحسينات أو اقتراحات
```

---

## 💡 **اقتراح ميزات**

### **نموذج طلب ميزة:**

```markdown
## عنوان الميزة
عنوان واضح وموجز

## المشكلة
ما المشكلة التي تحلها هذه الميزة؟

## الحل المقترح
وصف واضح للحل

## البدائل
ما البدائل الأخرى التي فكرت بها؟

## معلومات إضافية
سياق إضافي، mockups، أمثلة، إلخ.
```

---

## 🔍 **مراجعة الكود**

### **ما نبحث عنه:**

#### **✅ الجودة:**
- كود نظيف وواضح
- متبع للمعايير
- موثق بشكل جيد
- بدون كود متكرر

#### **✅ الاختبار:**
- تم الاختبار على أجهزة مختلفة
- لا يكسر الميزات الموجودة
- يعمل على Android 7+

#### **✅ الأداء:**
- لا يبطئ التطبيق
- لا يستهلك ذاكرة زائدة
- لا يستهلك بطارية زائدة

#### **✅ الأمان:**
- لا ثغرات أمنية
- معالجة صحيحة للصلاحيات
- حماية بيانات المستخدم

---

## 📦 **أنواع المساهمات**

### **1. إصلاح أخطاء**
```
Prefix: Fix:
مثال: Fix: Download resume not working on Android 11
```

### **2. ميزات جديدة**
```
Prefix: Add:
مثال: Add: Support for FTP downloads
```

### **3. تحسينات**
```
Prefix: Improve:
مثال: Improve: Download speed calculation accuracy
```

### **4. وثائق**
```
Prefix: Docs:
مثال: Docs: Add API documentation
```

### **5. إعادة هيكلة**
```
Prefix: Refactor:
مثال: Refactor: DownloadEngine for better performance
```

---

## 🧪 **الاختبار**

### **اختبار يدوي:**
```
✅ تنزيل ملف صغير (< 1 MB)
✅ تنزيل ملف متوسط (1-100 MB)
✅ تنزيل ملف كبير (> 100 MB)
✅ إيقاف واستئناف
✅ تنزيلات متزامنة
✅ WiFi/Mobile Data
✅ انقطاع الإنترنت
✅ إغلاق التطبيق
```

### **أجهزة الاختبار:**
```
✅ Android 7
✅ Android 10
✅ Android 13+
✅ أحجام شاشات مختلفة
```

---

## 📚 **الموارد**

### **للمطورين الجدد:**
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Android Developers](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)

### **بنية المشروع:**
```
راجع README.md لفهم بنية المشروع
```

---

## 🎯 **أولويات المساهمة**

### **عالية الأولوية:**
- 🔴 إصلاح أخطاء حرجة
- 🟠 تحسين الأداء
- 🟡 دعم Android الجديد

### **متوسطة الأولوية:**
- 🔵 ميزات جديدة
- 🟣 تحسين UI/UX
- 🟤 وثائق

### **منخفضة الأولوية:**
- ⚪ إعادة هيكلة
- ⚫ تحسينات طفيفة

---

## 💬 **التواصل**

### **للأسئلة:**
- افتح Discussion في GitHub
- لا تستخدم Issues للأسئلة

### **للمناقشات:**
- GitHub Discussions
- Pull Request Comments

---

## 📜 **رخصة المساهمة**

بالمساهمة في هذا المشروع، أنت توافق على:
- أن مساهمتك ستكون مفتوحة المصدر
- أن مساهمتك أصلية وليست منسوخة
- أن لديك الحق في المساهمة بهذا الكود

---

## 🏆 **المساهمون**

شكراً لجميع المساهمين! 🙏

```
سيتم إضافة قائمة المساهمين هنا
```

---

## 🎉 **شكراً!**

شكراً لمساهمتك في جعل **Pro Download Manager** أفضل!

كل مساهمة، صغيرة أو كبيرة، مهمة لنا. ❤️

---

<div align="center">
  <p><strong>Happy Coding! 🚀</strong></p>
</div>
