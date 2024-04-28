import com.github.britooo.looca.api.core.Looca;

public class LeituraRede {
    Looca looca = new Looca();
    Long redeDowload = looca.getRede().getGrupoDeInterfaces().getInterfaces().get(1).getBytesRecebidos();
    Long redeUpload = looca.getRede().getGrupoDeInterfaces().getInterfaces().get(1).getBytesEnviados();
}