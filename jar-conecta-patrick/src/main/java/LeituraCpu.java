import com.github.britooo.looca.api.core.Looca;
import oshi.SystemInfo;

public class LeituraCpu {
    WMICCommand zonaTermal = new WMICCommand();

    Looca looca = new Looca();
    SystemInfo oshi = new SystemInfo();
    Double cpuUso = looca.getProcessador().getUso();
    Double cpuCarga = oshi.getHardware().getProcessor().getSystemCpuLoad(1000) * 100;
    Double cpuTemperatura = zonaTermal.temp();
    Double getCpuTemperaturaFahrenheit = cpuTemperatura * 1.8 + 32;
    Double getCpuTemperaturaKelvin = cpuTemperatura + 273;

}
