@file:Suppress("DEPRECATION")

package com.fingerprintjs.android.fingerprint


import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.hardware.SensorManager
import android.hardware.input.InputManager
import android.media.RingtoneManager
import android.os.Environment
import android.os.StatFs
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.fingerprintjs.android.fingerprint.datasources.CpuInfoProvider
import com.fingerprintjs.android.fingerprint.datasources.CpuInfoProviderImpl
import com.fingerprintjs.android.fingerprint.datasources.DevicePersonalizationDataSource
import com.fingerprintjs.android.fingerprint.datasources.DevicePersonalizationDataSourceImpl
import com.fingerprintjs.android.fingerprint.datasources.FingerprintSensorInfoProvider
import com.fingerprintjs.android.fingerprint.datasources.FingerprintSensorInfoProviderImpl
import com.fingerprintjs.android.fingerprint.datasources.InputDeviceDataSource
import com.fingerprintjs.android.fingerprint.datasources.InputDevicesDataSourceImpl
import com.fingerprintjs.android.fingerprint.datasources.KeyGuardInfoProvider
import com.fingerprintjs.android.fingerprint.datasources.KeyGuardInfoProviderImpl
import com.fingerprintjs.android.fingerprint.datasources.MemInfoProvider
import com.fingerprintjs.android.fingerprint.datasources.MemInfoProviderImpl
import com.fingerprintjs.android.fingerprint.datasources.OsBuildInfoProvider
import com.fingerprintjs.android.fingerprint.datasources.OsBuildInfoProviderImpl
import com.fingerprintjs.android.fingerprint.datasources.PackageManagerDataSource
import com.fingerprintjs.android.fingerprint.datasources.PackageManagerDataSourceImpl
import com.fingerprintjs.android.fingerprint.datasources.SensorDataSource
import com.fingerprintjs.android.fingerprint.datasources.SensorDataSourceImpl
import com.fingerprintjs.android.fingerprint.datasources.SettingsDataSource
import com.fingerprintjs.android.fingerprint.datasources.SettingsDataSourceImpl
import com.fingerprintjs.android.fingerprint.device_id_providers.AndroidIdProvider
import com.fingerprintjs.android.fingerprint.device_id_providers.DeviceIdProvider
import com.fingerprintjs.android.fingerprint.device_id_providers.DeviceIdProviderImpl
import com.fingerprintjs.android.fingerprint.device_id_providers.GsfIdProvider
import com.fingerprintjs.android.fingerprint.signal_providers.device_state.DeviceStateSignalProvider
import com.fingerprintjs.android.fingerprint.signal_providers.hardware.HardwareSignalProvider
import com.fingerprintjs.android.fingerprint.signal_providers.installed_apps.InstalledAppsSignalProvider
import com.fingerprintjs.android.fingerprint.signal_providers.os_build.OsBuildSignalProvider
import com.fingerprintjs.android.fingerprint.tools.hashers.Hasher
import com.fingerprintjs.android.fingerprint.tools.hashers.MurMur3x64x128Hasher


object FingerprinterFactory {

    private var configuration: Configuration = Configuration(version = 1)
    private var instance: Fingerprinter? = null
    private var hasher: Hasher = MurMur3x64x128Hasher()

    @JvmStatic
    fun getInstance(
        context: Context,
        configuration: Configuration
    ): Fingerprinter {
        if (this.configuration != configuration) {
            instance = null
        }

        if (instance == null) {
            synchronized(FingerprinterFactory::class.java) {
                if (instance == null) {
                    instance = initializeFingerprinter(context, configuration)
                }
            }
        }

        return instance!!
    }

    private fun initializeFingerprinter(
        context: Context,
        configuration: Configuration
    ): Fingerprinter {
        this.configuration = configuration
        this.hasher = configuration.hasher

        return FingerprinterImpl(
            createHardwareFingerprinter(context),
            createOsBuildInfoFingerprinter(),
            createDeviceIdProvider(context),
            createInstalledApplicationsFingerprinter(context),
            createDeviceStateFingerprinter(context),
            configuration
        )
    }

    //region:Fingerprinters

    private fun createHardwareFingerprinter(context: Context): HardwareSignalProvider {
        return HardwareSignalProvider(
            createCpuInfoProvider(),
            createMemoryInfoProvider(context),
            createOsBuildInfoProvider(),
            createSensorDataSource(context),
            createInputDevicesDataSource(context),
            hasher,
            configuration.version
        )
    }

    private fun createOsBuildInfoFingerprinter(): OsBuildSignalProvider {
        return OsBuildSignalProvider(
            createOsBuildInfoProvider(),
            hasher,
            configuration.version
        )
    }

    private fun createInstalledApplicationsFingerprinter(context: Context): InstalledAppsSignalProvider {
        return InstalledAppsSignalProvider(
            createPackageManagerDataSource(context),
            hasher,
            configuration.version
        )
    }

    private fun createDeviceStateFingerprinter(context: Context): DeviceStateSignalProvider {
        return DeviceStateSignalProvider(
            createSettingsDataSource(context),
            createDevicePersonalizationDataSource(context),
            createKeyGuardInfoProvider(context),
            createFingerprintSensorStatusProvider(context),
            hasher,
            configuration.version
        )
    }

    private fun createDeviceIdProvider(context: Context): DeviceIdProvider {
        return DeviceIdProviderImpl(
            createGsfIdProvider(context),
            createAndroidIdProvider(context)
        )
    }

    //endregion

    //region:DataSources
    private fun createCpuInfoProvider(): CpuInfoProvider {
        return CpuInfoProviderImpl()
    }

    private fun createMemoryInfoProvider(context: Context): MemInfoProvider {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val internalStorageDir = Environment.getRootDirectory().absolutePath
        val internalStorageStatFs = StatFs(internalStorageDir)

        val externalStorageDir = context.getExternalFilesDir(null)?.absolutePath
        val externalStorageStatFs = if (externalStorageDir != null) StatFs(externalStorageDir) else null

        return MemInfoProviderImpl(activityManager, internalStorageStatFs, externalStorageStatFs)
    }

    private fun createOsBuildInfoProvider(): OsBuildInfoProvider {
        return OsBuildInfoProviderImpl()
    }

    private fun createGsfIdProvider(context: Context): GsfIdProvider {
        return GsfIdProvider(context.contentResolver!!)
    }

    private fun createAndroidIdProvider(context: Context): AndroidIdProvider {
        return AndroidIdProvider(context.contentResolver!!)
    }

    private fun createSensorDataSource(context: Context): SensorDataSource {
        return SensorDataSourceImpl(
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        )
    }

    private fun createInputDevicesDataSource(context: Context): InputDeviceDataSource {
        return InputDevicesDataSourceImpl(
            context.getSystemService(Context.INPUT_SERVICE) as InputManager
        )
    }

    private fun createPackageManagerDataSource(context: Context): PackageManagerDataSource {
        return PackageManagerDataSourceImpl(
            context.packageManager
        )
    }

    private fun createSettingsDataSource(context: Context): SettingsDataSource {
        return SettingsDataSourceImpl(context.contentResolver)
    }


    private fun createDevicePersonalizationDataSource(context: Context): DevicePersonalizationDataSource {
        return DevicePersonalizationDataSourceImpl(
            RingtoneManager(context),
            context.assets
        )
    }

    private fun createFingerprintSensorStatusProvider(context: Context): FingerprintSensorInfoProvider {
        return FingerprintSensorInfoProviderImpl(
            FingerprintManagerCompat.from(context)
        )
    }

    private fun createKeyGuardInfoProvider(context: Context): KeyGuardInfoProvider {
        return KeyGuardInfoProviderImpl(
            context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        )
    }

    //endregion
}