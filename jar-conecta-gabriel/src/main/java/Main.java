import com.github.britooo.looca.api.core.Looca;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import oshi.SystemInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner leitor = new Scanner(System.in);
        Conexao conexao = new Conexao();
        JdbcTemplate interfaceConexao = conexao.getConexaoDoBanco();
        FormatString leitura = new FormatString();

        Looca looca = new Looca();
        SystemInfo oshi = new SystemInfo();

        Path caminho = Paths.get("C:/Users/gabri/OneDrive/Documentos/usb.txt");
        String text = "Máquina:" + looca.getRede().getParametros().getHostName() + "\n\n";
        List<String> usbConexion = new ArrayList<>();

        for(int i = 0; i < oshi.getHardware().getUsbDevices(true).size(); i++){
            usbConexion.add("");
        }

//        verifica usuario
        System.out.println("Digite seu login");
        String login_digitado = leitor.nextLine();

        System.out.println("Digite sua senha");
        String senha_digitada = leitor.nextLine();

//        verifica se a máquina está cadastrada
        List<Usuario> usuarioBanco = interfaceConexao.query("SELECT * FROM Usuario WHERE emailUsuario = '%s' AND senhaUsuario = '%s'".formatted(login_digitado, senha_digitada), new BeanPropertyRowMapper<>(Usuario.class));

        switch (usuarioBanco.size()) {
            case 0:
//                usuario nao encontrado
                System.out.println("Login ou senha incorretos ou inexistentes");
                break;

            default:
                System.out.println("Login realizado, aguarde as leituras... \n\n\n\n\n\n");
//                 verifica se a máquina está cadastrada
                String hostname = looca.getRede().getParametros().getHostName();
                List<Maquina> maquinaBanco = interfaceConexao.query("SELECT * FROM Maquina WHERE hostnameMaquina = '%s'".formatted(hostname), new BeanPropertyRowMapper<>(Maquina.class));

                switch (maquinaBanco.size()) {
                    case 0:
//               maquina nao encontrada
                        System.out.println("Cadastre a máquina antes de prosseguir");
                        break;

                    default:
                        while (true) {

                            for(int i = 0; i < usbConexion.size(); i++){

                                if (!Objects.equals(oshi.getHardware().getUsbDevices(true).get(i).getConnectedDevices().toString(), usbConexion.get(i))){
                                    System.out.println("Novo dispositivo");
                                    System.out.println(oshi.getHardware().getUsbDevices(true).get(i).getConnectedDevices().toString());
                                    System.out.println(usbConexion.get(i));

                                    usbConexion.set(i, oshi.getHardware().getUsbDevices(true).get(i).getConnectedDevices().toString());

                                    LocalDateTime dateTime = LocalDateTime.now();
                                    DateTimeFormatter dateTimeTemplate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                                    String dateTimeFormatted = dateTime.format(dateTimeTemplate);
                                    text += dateTimeFormatted + "\n" + usbConexion.get(i) + "\n\n";
                                }

                            }

                            byte[] textByte = text.getBytes();
                            try{
                                Files.write(caminho, textByte);
                            } catch (Exception erro){
                                System.out.println("update in txt is not happening");
                            }


//                maquina encontrada, liberado enviar leitura
                            LeituraDisco discoAnterior = new LeituraDisco();
                            LeituraRede redeAnterior = new LeituraRede();

                            Thread.sleep(5000);

                            LeituraDisco discoAtual = new LeituraDisco();
                            LeituraRede redeAtual = new LeituraRede();

                            LeituraMemoria memoria = new LeituraMemoria();
                            LeituraCpu cpu = new LeituraCpu();

                            Long taxa_escrita_disco = ((discoAtual.discoTaxaEscrita - discoAnterior.discoTaxaEscrita) / 5) / 1024;  //kb/s
                            Long taxa_leitura_disco = ((discoAtual.discoTaxaLeitura - discoAnterior.discoTaxaLeitura) / 5) / 1024;  //kb/s

                            Long taxa_dowload_rede = ((redeAtual.redeDowload - redeAnterior.redeDowload) / 5) / 1024; //mb
                            Long taxa_upload_rede = ((redeAtual.redeUpload - redeAnterior.redeUpload) / 5) / 1024;    //mb

                            String fk_empresa = maquinaBanco.get(0).getFkEmpresaMaquina();
                            interfaceConexao.update("INSERT INTO LeituraDisco (discoDisponivel, discoTaxaLeitura, discoTaxaEscrita, fkComponenteDisco, fkMaquinaDisco)" +
                                    "VALUES (%s, %d, %d, 1, %s)".formatted
                                            (leitura.formatString(discoAtual.discoDisponivel), taxa_leitura_disco, taxa_escrita_disco, fk_empresa));

                            interfaceConexao.update("INSERT INTO LeituraMemoria (memoriaDisponivel, memoriaVirtual, tempoLigado, fkComponenteMemoria, fkMaquinaMemoria)" +
                                    "VALUES (%s, %s, %d, 2, %s)".formatted
                                            (leitura.formatString(memoria.memoriaDisponivel), leitura.formatString(memoria.memoriaVirtual), memoria.tempoLigado, fk_empresa));

                            interfaceConexao.update("INSERT INTO LeituraRede (redeDownload, redeUpload, fkComponenteRede, fkMaquinaRede)" +
                                    "VALUES (%d, %d, 3, %s)".formatted
                                            (taxa_dowload_rede, taxa_upload_rede, fk_empresa));

                            interfaceConexao.update("INSERT INTO LeituraCpu (cpuUso, cpuCarga, cpuTemperatura, fkComponenteCpu, fkMaquinaCpu)" +
                                    "VALUES (%s, %s, %s, 4, %s)".formatted
                                            (leitura.formatString(cpu.cpuUso), leitura.formatString(cpu.cpuCarga), leitura.formatString(cpu.cpuTemperatura), fk_empresa));

                            System.out.println("""
                                    \n\n\n\n\n\n
                                    Leituras realizadas com sucesso!
                                                        
                                    Enviando dados de disco:
                                    1 - Disco disponível: %.2f Gb
                                    2 - Taxa de escrita: %d Kb/s
                                    3 - Taxa de leitura: %d Kb/s
                                                        
                                    Enviando dados de memória:
                                    1 - Memória disponível: %.2f Gb
                                    2 - Memória virtual: %.2f Gb
                                    3 - Tempo ligado: %d Horas
                                                        
                                    Enviando dados de rede:
                                    1 - Taxa dowload: %d Mb/s
                                    2 - Taxa upload: %d Mb/s
                                                        
                                    Enviando dados de cpu:
                                    1 - Uso: %.2f %%
                                    2 - Carga: %.2f %%
                                    3 - Temperatura: %.2f °C
                                    """.formatted(
                                    discoAtual.discoDisponivel, taxa_escrita_disco, taxa_leitura_disco,
                                    memoria.memoriaDisponivel, memoria.memoriaVirtual, memoria.tempoLigado,
                                    taxa_dowload_rede, taxa_upload_rede,
                                    cpu.cpuUso, cpu.cpuCarga, cpu.cpuTemperatura
                            ));
                        }
                }
        }
    }
}
