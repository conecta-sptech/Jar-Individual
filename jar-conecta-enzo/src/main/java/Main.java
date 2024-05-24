import com.github.britooo.looca.api.core.Looca;
import org.json.JSONObject;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import oshi.SystemInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {

        Scanner leitor = new Scanner(System.in);
        Conexao conexao = new Conexao();
        JdbcTemplate interfaceConexao = conexao.getConexaoDoBanco();
        Looca looca = new Looca();
        SystemInfo oshi = new SystemInfo();
        FormatString leitura = new FormatString();
        Slack slack = new Slack();
        JSONObject messagemSlack = new JSONObject();


        String date = "";
        String logLevel = "";
        Integer statusCode = 0;
        String message = "";
        Integer idMaquina = 0;
        String hostnameMaquina = "";
        String stackTrace = "";
        String caminhoArquivo = "C:\\Users\\super\\Documents\\GIT\\Logs\\logs.txt";

        System.out.println("""
                              ----------------------------------------------
                                            SEJA BEM VINDO !!!
                                         
                                                CONECTA 💻
                                 "TRANSFORMANDO IDEIAS EM REDES DE SUCESSO"
                                
                               ----------------------------------------------
                             
                           Entre em sua conta  inserindo os dados nos campos abaixo:
                """);

        System.out.println("EMAIL:");
        String email_usuario = leitor.nextLine();

        System.out.println("SENHA:");
        String senha_usuario = leitor.nextLine();

        try {

            List<Usuario> usuarioBanco = interfaceConexao.query("SELECT * FROM Usuario WHERE emailUsuario = '%s' AND senhaUsuario = '%s'".formatted(email_usuario, senha_usuario), new BeanPropertyRowMapper<>(Usuario.class));

            switch (usuarioBanco.size()) {
                case 0:
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                    logLevel = "WARN";
                    statusCode = 401;
                    message = "E-mail ou senha incorreto(s).";

                    Log warnLogUsuario = new Log(date, logLevel, statusCode, message, stackTrace);
                    Log.gerarArquivoTxt(caminhoArquivo, warnLogUsuario.toStringMessage());

                    System.out.println("Login ou senha incorretos ou inexistentes !!!");
                    break;

                default:

                    System.out.println("Login realizado com sucesso, aguarde as leituras... \n\n\n\n\n\n");
                    String hostname = looca.getRede().getParametros().getHostName();
                    List<Maquina> maquinaBanco = interfaceConexao.query("SELECT * FROM Maquina WHERE hostnameMaquina = '%s'".formatted(hostname), new BeanPropertyRowMapper<>(Maquina.class));

                    switch (maquinaBanco.size()) {
                        case 0:
                            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                            logLevel = "WARN";
                            statusCode = 404;
                            message = "Máquina não encontrada no banco de dados.";

                            Log warnLogMaquina = new Log(date, logLevel, statusCode, idMaquina, hostname, message, stackTrace);
                            Log.gerarArquivoTxt(caminhoArquivo, warnLogMaquina.toStringMessage());
                            System.out.println("Cadastre a máquina antes de prosseguir");

                        default:

                            for (Maquina maquina : maquinaBanco) {
                                idMaquina = maquina.getIdMaquina();
                                hostnameMaquina = maquina.getHostnameMaquina();
                            }

                            while (true) {
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

                                // Alertas do Slack

                                if (cpu.cpuUso > 5.0) {
                                    messagemSlack.put("text", "Alerta: Processador " + cpu.cpuUso + "%");
                                } else if (memoria.memoriaDisponivel < 1.0) {
                                    messagemSlack.put("text", "Alerta: Memoria RAM " + memoria.memoriaDisponivel);
                                }
                                slack.sendMessage(messagemSlack);

                                // Inovação do projeto coletar dados de disco e mémoria em %

                                Double disco_disponivel_porcentagem = (discoAtual.discoDisponivel * 100 / looca.getGrupoDeDiscos().getDiscos().get(0).getTamanho() * Math.pow(1024.0, 3));
                                Double memoria_disponivel_porcentagem = (memoria.memoriaDisponivel / memoria.memoriaTotal) * 100;

                                System.out.println("_______________________________________________________________________");
                                System.out.println("""
                                         \n\n\
                                         
                                        Leituras realizadas com sucesso!
                                        Confira abaixo:
                                                                            
                                        --------------------------------------------                   
                                        Exibindo dados de Disco:
                                                                            
                                        1 - Disco disponível em %%: %.2f %%
                                        2 - Taxa de escrita: %d Kb/s
                                        3 - Taxa de leitura: %d Kb/s
                                                                            
                                        Exibindo dados de memória:
                                                                            
                                        1 - Memória disponível em %%: %.2f %%
                                        2 - Memória virtual: %.2f Gb
                                        3 - Tempo ligado: %d Horas
                                                                 
                                        Exibindo dados de Redes:
                                                                            
                                        1 - Taxa dowload: %d Mb/s
                                        2 - Taxa upload: %d Mb/
                                         
                                        Exibindo dados de CPU:
                                                                            
                                        1 - Uso: %.2f %%
                                        2 - Carga: %.2f %%
                                        3 - Temperatura: %.2f °C
                                                                            
                                        --------------------------------------------
                                                                            
                                        """.formatted(
                                        disco_disponivel_porcentagem, taxa_escrita_disco, taxa_leitura_disco,
                                        memoria_disponivel_porcentagem, memoria.memoriaVirtual, memoria.tempoLigado,
                                        taxa_dowload_rede, taxa_upload_rede,
                                        cpu.cpuUso, cpu.cpuCarga, cpu.cpuTemperatura
                                ));
                            }
                    }
            }
        } catch (Exception e) {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            logLevel = "ERROR";
            statusCode = 503;
            message = "Houve um problema de conexão com o banco de dados.";

            // Captura o stackTrace e o transforma em uma String
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            stackTrace = sw.toString().replace("\n", " ").replace("\r", "").replace("\t", "");

            Log errorLogServer = new Log(date, logLevel, statusCode, message, stackTrace);
            Log.gerarArquivoTxt(caminhoArquivo, errorLogServer.toStringMessage());
        }
    }
}