import com.github.britooo.looca.api.core.Looca;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WMICCommand {
    private final HardwareAbstractionLayer hardware;

    public WMICCommand() {
        hardware = new SystemInfo().getHardware();
    }

    public double temp() {
        Looca looca = new Looca();
        String sistema = looca.getSistema().getSistemaOperacional();
        if (sistema.toLowerCase().contains("windows")) {
            double temperature = Double.MIN_VALUE; // Inicialize com um valor padrão

            try {
                String command = "wmic /namespace:\\\\root\\wmi PATH MSAcpi_ThermalZoneTemperature get CurrentTemperature";
                Process process = Runtime.getRuntime().exec(command);

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Extrair a temperatura da linha e armazená-la
                    temperature = extractTemperature(line);
                    if (temperature != Double.MIN_VALUE) {
                        // Converter a temperatura para Celsius dividindo por 100
                        temperature /= 100;
                        break; // Sair do loop após extrair a temperatura
                    }
                }

                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.err.println(errorLine);
                }

                process.waitFor();

                reader.close();
                errorReader.close();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            return temperature;
        } else {
            return this.hardware.getSensors().getCpuTemperature();
        }
    }

    private double extractTemperature(String line) {
        // Remover espaços em branco extras e caracteres não numéricos
        line = line.trim().replaceAll("[^\\d.]", "");
        // Verificar se a string não está vazia
        if (!line.isEmpty()) {
            // Converter para double apenas se a string não estiver vazia
            return Double.parseDouble(line);
        } else {
            // Retornar um valor padrão se a linha estiver vazia
            return Double.MIN_VALUE;
        }
    }
}