package br.com.system.UmaMensagemDiaSemana;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.core.JapeSession;

public class InsereUsuarios implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        JapeSession.SessionHandle hnd = null;
        Registro[] registros = ctx.getLinhas();
        if (registros.length == 1) {
            try {
                hnd = JapeSession.open(); 

                // Obter o registro atual
                Registro registro = registros[0];

                Object codUsu = ctx.getParam("CODUSU");
                String codUsuStr = codUsu.toString();

                // Obter o campo "USUARIOS"
                String usuariosAtuais = (String) registro.getCampo("USUARIOS");

                // Inicializar usuariosAtuais se for nulo
                if (usuariosAtuais == null) {
                    usuariosAtuais = "";
                }
                
                // Verificar se o usuário já está na lista
                String[] usuariosArray = usuariosAtuais.isEmpty() ? new String[0] : usuariosAtuais.split(";");
                boolean usuarioExiste = false;
                for (String usuario : usuariosArray) {
                    if (usuario.equals(codUsuStr)) {
                        usuarioExiste = true;
                        break;
                    }
                }

                if (usuarioExiste) {
                    ctx.setMensagemRetorno("Usuário já está na lista: " + codUsuStr);
                } else {
                    // Adicionar o novo usuário
                    String novosUsuarios;
                    if (usuariosAtuais.isEmpty()) {
                        novosUsuarios = codUsuStr;
                    } else {
                        novosUsuarios = usuariosAtuais + ";" + codUsuStr;
                    }

                    // Definir o campo "USUARIOS" com o novo valor
                    registro.setCampo("USUARIOS", novosUsuarios);

                    // Salvar o registro
                    registro.save();

                    // Definir a mensagem de retorno no contexto
                    ctx.setMensagemRetorno("Usuário inserido: " + codUsuStr);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                ctx.setMensagemRetorno("Erro ao inserir usuário: " + e.getMessage());
            } finally {
                JapeSession.close(hnd); // Fechar a sessão Jape
            }
                        
        } else {            
            ctx.setMensagemRetorno("Selecione um registro por vez.");
        }
    }
}
