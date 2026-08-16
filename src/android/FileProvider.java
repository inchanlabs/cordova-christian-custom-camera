<provider
    android:name="com.christian.customcamera.FileProvider"
    android:authorities="${applicationId}.customcamera.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">

    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/custom_camera_paths" />

</provider>
