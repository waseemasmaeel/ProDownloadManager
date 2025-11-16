# دليل البناء السريع - Pro Download Manager

## 📱 **بناء APK بخطوات سريعة**

### **الطريقة 1: باستخدام Android Studio (موصى بها)**

1. **افتح المشروع:**
   ```
   Android Studio > File > Open > اختر مجلد ProDownloadManager
   ```

2. **انتظر مزامنة Gradle:**
   - سيظهر شريط تقدم في الأسفل
   - انتظر حتى يكتمل التحميل (قد يستغرق 2-5 دقائق)

3. **بناء APK:**
   ```
   Build > Build Bundle(s) / APK(s) > Build APK(s)
   ```

4. **احصل على الملف:**
   - سيظهر إشعار في الأسفل
   - اضغط على "locate" للوصول إلى الملف
   - المسار: `app/build/outputs/apk/debug/app-debug.apk`

---

### **الطريقة 2: باستخدام سطر الأوامر**

#### **في Windows:**
```bash
cd ProDownloadManager
gradlew.bat assembleDebug
```

#### **في Mac/Linux:**
```bash
cd ProDownloadManager
chmod +x gradlew
./gradlew assembleDebug
```

---

## 📋 **متطلبات البناء**

✅ **Android Studio Hedgehog (2023.1.1)** أو أحدث  
✅ **JDK 17**  
✅ **اتصال بالإنترنت** (لتحميل التبعيات في المرة الأولى)  
✅ **مساحة تخزين**: ~2 GB

---

## 🔧 **حل المشاكل الشائعة**

### **خطأ: "SDK not found"**
```
File > Project Structure > SDK Location
تأكد من تحديد مسار Android SDK الصحيح
```

### **خطأ: "Gradle sync failed"**
```
File > Invalidate Caches > Invalidate and Restart
```

### **خطأ: "Out of memory"**
أضف في ملف `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
```

---

## 🚀 **تثبيت التطبيق**

### **على هاتف حقيقي:**
1. فعّل "Developer Options" على الهاتف
2. فعّل "USB Debugging"
3. صِل الهاتف بالكمبيوتر
4. في Android Studio: `Run > Run 'app'`

### **أو انقل APK يدوياً:**
1. انسخ ملف `app-debug.apk`
2. انقله إلى هاتفك
3. افتحه وثبته
4. قد تحتاج لتفعيل "Install from unknown sources"

---

## 📦 **بناء إصدار Release (موقّع)**

1. **أنشئ Keystore:**
   ```
   Build > Generate Signed Bundle / APK
   > Create new...
   ```

2. **املأ المعلومات:**
   - Key store path
   - Password
   - Alias
   - Validity (years)

3. **بناء Release APK:**
   ```
   Build > Generate Signed Bundle / APK
   > APK > Next
   > اختر الـ Keystore
   > Release > Finish
   ```

---

## 💡 **نصائح**

- **أول بناء** قد يستغرق وقتاً طويلاً (10-15 دقيقة)
- **البناءات التالية** ستكون أسرع بكثير
- احتفظ بنسخة من **Keystore** في مكان آمن
- استخدم **Build Variants** لتبديل بين Debug و Release

---

## 📊 **أحجام APK المتوقعة**

- **Debug APK**: ~15-20 MB
- **Release APK** (بدون minify): ~12-15 MB
- **Release APK** (مع minify): ~8-10 MB

---

## ✅ **التحقق من نجاح البناء**

بعد البناء، يجب أن ترى:
```
BUILD SUCCESSFUL in Xs
```

إذا رأيت أخطاء، راجع قسم "حل المشاكل الشائعة" أعلاه.

---

**جاهز للاستخدام! 🎉**
