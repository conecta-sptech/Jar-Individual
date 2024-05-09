import com.github.britooo.looca.api.core.Looca;
import oshi.SystemInfo;

public class LeituraCpu {
    WMICCommand zonaTermal = new WMICCommand();
    Main main = new Main();
    Looca looca = new Looca();
    SystemInfo oshi = new SystemInfo();
    Double cpuUso = looca.getProcessador().getUso();
    Double cpuCarga = oshi.getHardware().getProcessor().getSystemCpuLoad(1000) * 100;
    Double cpuTemperatura = zonaTermal.temp();
    Double getCpuTemperaturaFahrenheit = cpuTemperatura * 1.8 + 32;
    Double getCpuTemperaturaKelvin = cpuTemperatura + 273;
    Double temp = looca.getTemperatura().getTemperatura();

    public static class TemperatureHandler {
        public static Double handleTemperature(Double temperature) {
            // Aqui você pode implementar a lógica para manipular a temperatura
            System.out.println("Temperature received: " + temperature);
            return temperature;
            // Por exemplo, você pode chamar métodos de outra classe, etc.
        }
    }
}
