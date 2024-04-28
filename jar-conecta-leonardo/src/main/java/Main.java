import com.github.britooo.looca.api.core.Looca;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner in = new Scanner(System.in);
        Conexao conexao = new Conexao();
        JdbcTemplate interfaceConexao = conexao.getConexaoDoBanco();

        Looca looca = new Looca();

        FormatString leitura = new FormatString();

        System.out.println("Digite seu e-mail:");
        String email = in.nextLine();

        System.out.println("Digite sua senha:");
        String senha = in.nextLine();

        List<Usuario> usuarioBanco = interfaceConexao.query("SELECT * FROM Usuario WHERE emailUsuario = '%s' AND senhaUsuario = '%s'".formatted(email, senha), new BeanPropertyRowMapper<>(Usuario.class));

        switch (usuarioBanco.size()) {
            case 0:
                System.out.println("Login ou senha incorretos ou inexistentes.");
                break;

            default:
                System.out.println("Digite o tempo de atualização das leituras de dados:");
                Integer tempoAtualizacao = in.nextInt();

                if (tempoAtualizacao < 20) {
                    tempoAtualizacao = 20;
                    System.out.println("\nO tempo de atualização deve ser de pelo menos 20 segundos ou mais! \n-- Tempo de atualização alterado para o padrão de 20 segundos --\n");
                }

                tempoAtualizacao -= 5;

                System.out.println("Login realizado, aguarde as leituras...");
                String hostname = looca.getRede().getParametros().getHostName();
                List<Maquina> maquinaBanco = interfaceConexao.query("SELECT * FROM Maquina WHERE hostnameMaquina = '%s'".formatted(hostname), new BeanPropertyRowMapper<>(Maquina.class));

                switch (maquinaBanco.size()) {
                    case 0:
                        System.out.println("Cadastre a máquina antes de prosseguir.");
                        break;

                    default:
                        while (true) {
                            LeituraDisco discoAnterior = new LeituraDisco();
                            LeituraRede redeAnterior = new LeituraRede();

                            Thread.sleep(tempoAtualizacao * 1000);

                            LeituraDisco discoAtual = new LeituraDisco();
                            LeituraRede redeAtual = new LeituraRede();

                            LeituraMemoria memoria = new LeituraMemoria();
                            LeituraCpu cpu = new LeituraCpu();

                            Long taxaEscritaDisco = ((discoAtual.discoTaxaEscrita - discoAnterior.discoTaxaEscrita) / tempoAtualizacao) / 1024;  //kb/s
                            Long taxaLeituraDisco = ((discoAtual.discoTaxaLeitura - discoAnterior.discoTaxaLeitura) / tempoAtualizacao) / 1024;  //kb/s

                            Long taxaDowloadRede = ((redeAtual.redeDowload - redeAnterior.redeDowload) / tempoAtualizacao) / 1024; //mb
                            Long taxaUploadRede = ((redeAtual.redeUpload - redeAnterior.redeUpload) / tempoAtualizacao) / 1024;    //mb

                            String fk_empresa = maquinaBanco.get(0).getFkEmpresaMaquina();
                            interfaceConexao.update("INSERT INTO LeituraDisco (discoDisponivel, discoTaxaLeitura, discoTaxaEscrita, fkComponenteDisco, fkMaquinaDisco)" +
                                    "VALUES (%s, %d, %d, 1, %s)".formatted
                                            (leitura.formatString(discoAtual.discoDisponivel), taxaLeituraDisco, taxaEscritaDisco, fk_empresa));

                            interfaceConexao.update("INSERT INTO LeituraMemoria (memoriaDisponivel, memoriaVirtual, tempoLigado, fkComponenteMemoria, fkMaquinaMemoria)" +
                                    "VALUES (%s, %s, %d, 2, %s)".formatted
                                            (leitura.formatString(memoria.memoriaDisponivel), leitura.formatString(memoria.memoriaVirtual), memoria.tempoLigado, fk_empresa));

                            interfaceConexao.update("INSERT INTO LeituraRede (redeDownload, redeUpload, fkComponenteRede, fkMaquinaRede)" +
                                    "VALUES (%d, %d, 3, %s)".formatted
                                            (taxaDowloadRede, taxaUploadRede, fk_empresa));

                            interfaceConexao.update("INSERT INTO LeituraCpu (cpuUso, cpuCarga, cpuTemperatura, fkComponenteCpu, fkMaquinaCpu)" +
                                    "VALUES (%s, %s, %s, 4, %s)".formatted
                                            (leitura.formatString(cpu.cpuUso), leitura.formatString(cpu.cpuCarga), leitura.formatString(cpu.cpuTemperatura), fk_empresa));

                            System.out.println("""
                                    \n
                                    Leituras realizadas com sucesso!
                                                        
                                    Enviando dados de Disco:
                                    1 - Disco disponível: %.2f Gb
                                    2 - Taxa de escrita: %d Kb/s
                                    3 - Taxa de leitura: %d Kb/s
                                                        
                                    Enviando dados de Memória:
                                    1 - Memória disponível: %.2f Gb
                                    2 - Memória virtual: %.2f Gb
                                    3 - Tempo ligado: %d Horas
                                                        
                                    Enviando dados de Rede:
                                    1 - Taxa dowload: %d Mb/s
                                    2 - Taxa upload: %d Mb/s
                                                        
                                    Enviando dados de CPU:
                                    1 - Uso: %.2f %%
                                    2 - Carga: %.2f %%
                                    3 - Temperatura: %.2f °C
                                    """.formatted(
                                    discoAtual.discoDisponivel, taxaEscritaDisco, taxaLeituraDisco,
                                    memoria.memoriaDisponivel, memoria.memoriaVirtual, memoria.tempoLigado,
                                    taxaDowloadRede, taxaUploadRede,
                                    cpu.cpuUso, cpu.cpuCarga, cpu.cpuTemperatura
                            ));
                        }
                }
        }
    }
}