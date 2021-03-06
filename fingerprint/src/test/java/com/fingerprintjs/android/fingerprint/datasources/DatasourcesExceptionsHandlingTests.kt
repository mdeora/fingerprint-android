package com.fingerprintjs.android.fingerprint.datasources


import com.fingerprintjs.android.fingerprint.device_id_providers.AndroidIdProvider
import com.fingerprintjs.android.fingerprint.device_id_providers.DeviceIdProviderImpl
import com.fingerprintjs.android.fingerprint.device_id_providers.GsfIdProvider
import com.nhaarman.mockitokotlin2.mock
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Test

// Check that there is no unhandled exceptions. Calling mock methods produces NPE.
class DatasourcesExceptionsHandlingTests {
    @Test
    fun `CPUInfo provider crash free`() {
        val cpuInfoProvider = CpuInfoProviderImpl()
        assertNotNull(cpuInfoProvider.cpuInfo().size)
    }

    @Test
    fun `Memory Info provider crash free`() {
        val memInfoProvider = MemInfoProviderImpl(
            mock(),
            mock(),
            mock()
        )

        assertEquals(0, memInfoProvider.totalRAM())
        assertEquals(0, memInfoProvider.totalExternalStorageSpace())
        assertEquals(0, memInfoProvider.totalInternalStorageSpace())
    }

    @Test
    fun `DevicePersonalizationDataSource crash free`() {
        val devicePersonalizationDataSource = DevicePersonalizationDataSourceImpl(
            mock(),
            mock()
        )
        assertEquals("", devicePersonalizationDataSource.ringtoneSource())
        assertEquals(0, devicePersonalizationDataSource.availableLocales().size)
    }

    @Test
    fun `InputDeviceDataSource crash free`() {
        val inputDeviceDatasource = InputDevicesDataSourceImpl(
            mock()
        )
        assertEquals(0, inputDeviceDatasource.getInputDeviceData().size)
    }

    @Test
    fun `SensorsDataSource crash free`() {
        val sensorsDataSource = SensorDataSourceImpl(
            mock()
        )
        assertEquals(0, sensorsDataSource.sensors().size)
    }

    @Test
    fun `PackageManager datasource crash free`() {
        val packageManagerDataSource = PackageManagerDataSourceImpl(
            mock()
        )
        assertEquals(0, packageManagerDataSource.getApplicationsList().size)
    }

    @Test
    fun `KeyGuardInfoProvider datasource crash free`() {
        val keyGuardInfoProvider = KeyGuardInfoProviderImpl(
            mock()
        )
        assertEquals(false, keyGuardInfoProvider.isPinSecurityEnabled())
    }

    @Test
    fun `SettingsDataSource crash free`() {
        val settingsDataSource = SettingsDataSourceImpl(mock())
        assertEquals("", settingsDataSource.accessibilityEnabled())
        assertEquals("", settingsDataSource.adbEnabled())
        assertEquals("", settingsDataSource.alarmAlertPath())
        assertEquals("", settingsDataSource.dataRoamingEnabled())
        assertEquals("", settingsDataSource.dateFormat())
        assertEquals("", settingsDataSource.defaultInputMethod())
        assertEquals("", settingsDataSource.developmentSettingsEnabled())
        assertEquals("", settingsDataSource.endButtonBehaviour())
        assertEquals("", settingsDataSource.fontScale())
        assertEquals("", settingsDataSource.httpProxy())
        assertEquals("", settingsDataSource.rttCallingMode())
        assertEquals("", settingsDataSource.screenOffTimeout())
        assertEquals("", settingsDataSource.textAutoPunctuate())
        assertEquals("", settingsDataSource.textAutoReplaceEnable())
        assertEquals("", settingsDataSource.time12Or24())
        assertEquals("", settingsDataSource.touchExplorationEnabled())
        assertEquals("", settingsDataSource.transitionAnimationScale())
        assertEquals("", settingsDataSource.windowAnimationScale())
    }

    @Test
    fun `DeviceId crash free`() {
        val deviceIdProvider = DeviceIdProviderImpl(
            GsfIdProvider(mock()),
            AndroidIdProvider(mock())
        )
        assertEquals("", deviceIdProvider.getDeviceId())
    }

}