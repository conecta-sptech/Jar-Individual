import com.github.britooo.looca.api.core.Looca;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import oshi.SystemInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner leitor = new Scanner(System.in);
        Scanner leitorOp = new Scanner(System.in);
        Scanner leitorConf = new Scanner(System.in);
        Conexao conexao = new Conexao();
        JdbcTemplate interfaceConexao = conexao.getConexaoDoBanco();
        ArrayList<String> dadosSelecionados = new ArrayList<>();

        Looca looca = new Looca();
        SystemInfo oshi = new SystemInfo();

        FormatString leitura = new FormatString();
        Integer opcao;
        System.out.println("""
                              ----------------------------------------------
                                          Bem-vindo à Conecta
                                 "Transformando Ideias em Redes de Sucesso"
                               ----------------------------------------------
        """);

        do {
            System.out.println("Menu:");
            System.out.println("1. Sobre a Conecta");
            System.out.println("2. Componentes");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");
            opcao = leitorOp.nextInt();

            switch (opcao) {
                case 1:
                    System.out.println("\nSobre a Conecta:");
                    System.out.println("Somos especializados em monitoramento de serviços hospitalares.");
                    System.out.println("Nossa missão é fornecer soluções inovadoras para a gestão eficiente de hospitais.\n");
                    break;
                case 2:
                    monitorarComponente();
                    break;
                case 0:
                    System.out.println("\nSaindo do Menu. Até logo!");
                    break;
                default:
                    System.out.println("\nOpção inválida. Por favor, escolha uma opção válida.");
                    break;
            }
        } while (opcao != 0);

        System.out.println("\nInsira os dados nos campos abaixo para conectar em sua conta");
        System.out.println("EMAIL:");
        String email_usuario = leitor.nextLine();

        System.out.println("SENHA:");
        String senha_usuario = leitor.nextLine();


        List<Usuario> usuarioBanco = interfaceConexao.query("SELECT * FROM Usuario WHERE emailUsuario = '%s' AND senhaUsuario = '%s'".formatted(email_usuario, senha_usuario), new BeanPropertyRowMapper<>(Usuario.class));
        switch (usuarioBanco.size()) {
            case 0:
                System.out.println("Login ou senha incorretos ou inexistentes !!!");
                break;

            default:
                System.out.println("Login realizado com sucesso, aguarde as leituras... \n\n");

                int opcaoConf = 0;

                System.out.println("Configuração das leituras do dados:");
                System.out.println("1. Leitura padrão");
                System.out.println("2. Personalizar Leitura");
                System.out.print("Escolha uma opção: ");
                opcao = leitorConf.nextInt();

                switch (opcao) {
                    case 1:
                        opcaoConf = 1;
                        break;
                    case 2:
                        opcaoConf = 2;
                        escolherComponente(dadosSelecionados);
                        break;
                    default:
                        System.out.println("\nOpção inválida. Por favor, escolha uma opção válida.");
                        break;
                }

                String hostname = looca.getRede().getParametros().getHostName();
                List<Maquina> maquinaBanco = interfaceConexao.query("SELECT * FROM Maquina WHERE hostnameMaquina = '%s'".formatted(hostname), new BeanPropertyRowMapper<>(Maquina.class));
                switch (maquinaBanco.size()) {
                    case 0:
                        System.out.println("Cadastre a máquina antes de prosseguir");
                        break;
                    default:
                        if(opcaoConf == 1){
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


                                System.out.println("""
                                         \n
                                                                            
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
                                        discoAtual.discoDisponivel, taxa_escrita_disco, taxa_leitura_disco,
                                        memoria.memoriaDisponivel, memoria.memoriaVirtual, memoria.tempoLigado,
                                        taxa_dowload_rede, taxa_upload_rede,
                                        cpu.cpuUso, cpu.cpuCarga, cpu.cpuTemperatura
                                ));
                            }
                        }else{
                            System.out.println("Dados selecionados:");
                            for (String dado : dadosSelecionados) {
                                System.out.println("- " + dado);
                            }
                        }
                }
        }
    }

    public static void monitorarComponente() {
        Scanner leitor = new Scanner(System.in);
        Integer componente;
        do {
            System.out.println("\nComponentes:");
            System.out.println("Escolha o componente:");
            System.out.println("1. CPU");
            System.out.println("2. Memória");
            System.out.println("3. Rede");
            System.out.println("4. Disco");
            System.out.println("0. Sair");
            componente = leitor.nextInt();
            switch (componente) {
                case 1:
                    System.out.println("Dados que serão capturados da CPU:");
                    System.out.println("• Uso da CPU (%)");
                    System.out.println("• Carga (%)");
                    System.out.println("• Temperatura (°C)");
                    break;
                case 2:
                    System.out.println("Dados que serão capturados da Memória:");
                    System.out.println("• Memória Disponível (%)");
                    System.out.println("• Memória virtual (Gb)");
                    System.out.println("• Tempo ligado (Horas)");
                    break;
                case 3:
                    System.out.println("Dados que serão capturados da Rede:");
                    System.out.println("• Taxa Dowload (Mb/s)");
                    System.out.println("• Taxa Upload (Mb/s)");
                    break;
                case 4:
                    System.out.println("Dados que serão capturados do Disco:");
                    System.out.println("• Disco Disponível (%)");
                    System.out.println("• Taxa de Escrita (Kb/s)");
                    System.out.println("• Taxa de Leitura (Kb/s)");
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        }while (componente != 0);
    }


    public static void escolherComponente( ArrayList<String> dadosSelecionados) throws InterruptedException {
        Scanner leitor = new Scanner(System.in);
        Integer componente;

        do {
            System.out.println("Escolha o componente:");
            System.out.println("1. CPU");
            System.out.println("2. Memória");
            System.out.println("3. Disco");
            System.out.println("4. Rede");
            System.out.println("0. Concluir");
            componente = leitor.nextInt();

            switch (componente) {
                case 1:
                    selecionarDadosCPU(dadosSelecionados);
                    break;
                case 2:
                    selecionarDadosMemoria(dadosSelecionados);
                    break;
                case 3:
                    selecionarDadosDisco(dadosSelecionados);
                    break;
                case 4:
                    selecionarDadosRede(dadosSelecionados);
                    break;
                case 0:
                    System.out.println("Concluindo seleção...");
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        } while (componente != 0);
    }


    public static void selecionarDadosCPU(ArrayList<String> dadosSelecionados) {
        Scanner leitor = new Scanner(System.in);
        int opcaoCPU;
        Conexao conexao = new Conexao();
        Looca looca = new Looca();
        JdbcTemplate interfaceConexao = conexao.getConexaoDoBanco();
        FormatString leitura = new FormatString();
        LeituraCpu cpu = new LeituraCpu();
        String hostname = looca.getRede().getParametros().getHostName();
        List<Maquina> maquinaBanco = interfaceConexao.query("SELECT * FROM Maquina WHERE hostnameMaquina = '%s'".formatted(hostname), new BeanPropertyRowMapper<>(Maquina.class));
        String fk_empresa = maquinaBanco.get(0).getFkEmpresaMaquina();
        interfaceConexao.update("INSERT INTO LeituraCpu (cpuUso, cpuCarga, cpuTemperatura, fkComponenteCpu, fkMaquinaCpu)" +
                "VALUES (%s, %s, %s, 4, %s)".formatted
                        (leitura.formatString(cpu.cpuUso), leitura.formatString(cpu.cpuCarga), leitura.formatString(cpu.cpuTemperatura), fk_empresa));

        do {
            System.out.println("Dados que serão capturados da CPU:");
            System.out.println("1. Uso da CPU (%)");
            System.out.println("2. Carga (%)");
            System.out.println("3. Temperatura (°C)");
            System.out.println("0. Concluir seleção");

            opcaoCPU = leitor.nextInt();

            switch (opcaoCPU) {
                case 1:
                    dadosSelecionados.add(""" 
                            Uso da CPU:  %.2f %%""".formatted(cpu.cpuUso));
                    break;
                case 2:
                    dadosSelecionados.add("""
                            Carga da CPU: %.2f %%""".formatted(cpu.cpuCarga));
                    break;
                case 3:
                    dadosSelecionados.add("""
                            Temperatura da CPU: %.2f °C""".formatted(cpu.cpuTemperatura));
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        } while (opcaoCPU != 0);
    }

    public static void selecionarDadosMemoria(ArrayList<String> dadosSelecionados) {
        Scanner leitor = new Scanner(System.in);
        int opcaoMemoria;

        Conexao conexao = new Conexao();
        Looca looca = new Looca();
        JdbcTemplate interfaceConexao = conexao.getConexaoDoBanco();
        FormatString leitura = new FormatString();
        LeituraMemoria memoria = new LeituraMemoria();
        String hostname = looca.getRede().getParametros().getHostName();
        List<Maquina> maquinaBanco = interfaceConexao.query("SELECT * FROM Maquina WHERE hostnameMaquina = '%s'".formatted(hostname), new BeanPropertyRowMapper<>(Maquina.class));
        String fk_empresa = maquinaBanco.get(0).getFkEmpresaMaquina();
        interfaceConexao.update("INSERT INTO LeituraMemoria (memoriaDisponivel, memoriaVirtual, tempoLigado, fkComponenteMemoria, fkMaquinaMemoria)" +
                "VALUES (%s, %s, %d, 2, %s)".formatted
                        (leitura.formatString(memoria.memoriaDisponivel), leitura.formatString(memoria.memoriaVirtual), memoria.tempoLigado, fk_empresa));

        do {
            System.out.println("Dados que serão capturados da Memória:");
            System.out.println("1. Memória Disponível (%)");
            System.out.println("2. Memória virtual (Gb)");
            System.out.println("3. Tempo ligado (Horas)");
            System.out.println("0. Concluir seleção");

            opcaoMemoria = leitor.nextInt();

            System.out.println("Memoria");
            switch (opcaoMemoria) {
                case 1:
                    dadosSelecionados.add("""
                            Memória Disponível: %.2f %%""".formatted(memoria.memoriaDisponivel));
                    break;
                case 2:
                    dadosSelecionados.add("""
                            Memória virtual: %.2f Gb""".formatted(memoria.memoriaVirtual));
                    break;
                case 3:
                    dadosSelecionados.add("""
                            Tempo ligado da memoria:""".formatted(memoria.tempoLigado));
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        } while (opcaoMemoria != 0);
    }


    public static void selecionarDadosDisco(ArrayList<String> dadosSelecionados) throws InterruptedException {
        Scanner leitor = new Scanner(System.in);
        int opcaoDisco;
        Conexao conexao = new Conexao();
        Looca looca = new Looca();
        JdbcTemplate interfaceConexao = conexao.getConexaoDoBanco();
        FormatString leitura = new FormatString();

        LeituraDisco discoAnterior = new LeituraDisco();

        Thread.sleep(5000);

        LeituraDisco discoAtual = new LeituraDisco();
        Long taxa_escrita_disco = ((discoAtual.discoTaxaEscrita - discoAnterior.discoTaxaEscrita) / 5) / 1024;  //kb/s
        Long taxa_leitura_disco = ((discoAtual.discoTaxaLeitura - discoAnterior.discoTaxaLeitura) / 5) / 1024;  //kb/s

        String hostname = looca.getRede().getParametros().getHostName();
        List<Maquina> maquinaBanco = interfaceConexao.query("SELECT * FROM Maquina WHERE hostnameMaquina = '%s'".formatted(hostname), new BeanPropertyRowMapper<>(Maquina.class));
        String fk_empresa = maquinaBanco.get(0).getFkEmpresaMaquina();
        interfaceConexao.update("INSERT INTO LeituraDisco (discoDisponivel, discoTaxaLeitura, discoTaxaEscrita, fkComponenteDisco, fkMaquinaDisco)" +
                "VALUES (%s, %d, %d, 1, %s)".formatted
                        (leitura.formatString(discoAtual.discoDisponivel), taxa_leitura_disco, taxa_escrita_disco, fk_empresa));

        do {
            System.out.println("Dados que serão capturados do Disco:");
            System.out.println("1. Disco Disponível (%)");
            System.out.println("2. Taxa de Escrita (Kb/s)");
            System.out.println("3. Taxa de Leitura (Kb/s)");
            System.out.println("0. Concluir seleção");

            opcaoDisco = leitor.nextInt();

            System.out.println("Disco");
            switch (opcaoDisco) {
                case 1:
                    dadosSelecionados.add("""
                            Disco disponível em %%: %.2f %%""".formatted(discoAtual.discoDisponivel));
                    break;
                case 2:
                    dadosSelecionados.add("""
                            Taxa de escrita: %d Kb/s""".formatted(taxa_escrita_disco));
                    break;
                case 3:
                    dadosSelecionados.add("""
                            Taxa de leitura: %d Kb/s""".formatted(taxa_leitura_disco));
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        } while (opcaoDisco != 0);
    }


    public static void selecionarDadosRede(ArrayList<String> dadosSelecionados) throws InterruptedException {
        Scanner leitor = new Scanner(System.in);
        int opcaoDisco;
        Conexao conexao = new Conexao();
        Looca looca = new Looca();
        JdbcTemplate interfaceConexao = conexao.getConexaoDoBanco();

        LeituraRede redeAnterior = new LeituraRede();

        Thread.sleep(5000);

        LeituraRede redeAtual = new LeituraRede();
        Long taxa_dowload_rede = ((redeAtual.redeDowload - redeAnterior.redeDowload) / 5) / 1024; //mb
        Long taxa_upload_rede = ((redeAtual.redeUpload - redeAnterior.redeUpload) / 5) / 1024;    //mb

        String hostname = looca.getRede().getParametros().getHostName();
        List<Maquina> maquinaBanco = interfaceConexao.query("SELECT * FROM Maquina WHERE hostnameMaquina = '%s'".formatted(hostname), new BeanPropertyRowMapper<>(Maquina.class));
        String fk_empresa = maquinaBanco.get(0).getFkEmpresaMaquina();
        interfaceConexao.update("INSERT INTO LeituraRede (redeDownload, redeUpload, fkComponenteRede, fkMaquinaRede)" +
                "VALUES (%d, %d, 3, %s)".formatted
                        (taxa_dowload_rede, taxa_upload_rede, fk_empresa));

        do {
            System.out.println("Dados que serão capturados de Redes:");
            System.out.println("1. Taxa Dowload (Mb/s)");
            System.out.println("2. Taxa Upload (Mb/s)");
            System.out.println("0. Concluir seleção");

            opcaoDisco = leitor.nextInt();

            System.out.println("Rede");
            switch (opcaoDisco) {
                case 1:
                    dadosSelecionados.add("""
                            Taxa dowload: %d Mb/s""".formatted(taxa_dowload_rede));
                    break;
                case 2:
                    dadosSelecionados.add("""
                            Taxa upload: %d Mb/s""".formatted(taxa_upload_rede));
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        } while (opcaoDisco != 0);
    }
}

