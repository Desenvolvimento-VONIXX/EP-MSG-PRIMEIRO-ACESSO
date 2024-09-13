package br.com.system.msgInicioTrabalho;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import com.sankhya.util.JdbcUtils;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

public class EventMsgInicial implements EventoProgramavelJava {

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent event) throws Exception {
		
		DynamicVO sepVO = (DynamicVO) event.getVo();

	    BigDecimal codUsu = sepVO.asBigDecimal("CODUSU"); 
	    
	    JapeSession.SessionHandle hnd = null;
	    JdbcWrapper jdbc = null;
	    NativeSql sql = null;
	    ResultSet rset = null;
	    
	    try {
	        hnd = JapeSession.open();
	        hnd.setFindersMaxRows(-1);
	        final EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	        jdbc = entity.getJdbcWrapper();
	        jdbc.openSession();
	        sql = new NativeSql(jdbc);
	        sql.appendSql("SELECT \r\n"
	        		+ "COUNT(CODUSU) AS CONTAGEM\r\n"
	        		+ "FROM sankhya.TSIRLG  RLG WHERE  RLG.CODUSU = :CODUSULOGADO AND CONVERT(DATE,LOGIN) = CONVERT(DATE,GETDATE())");
	        
			sql.setNamedParameter("CODUSULOGADO", codUsu);

	        
	        rset = sql.executeQuery();
	        

	        while (rset.next()) {
	            try {
	                BigDecimal countLogin = rset.getBigDecimal("CONTAGEM");
	               
	                //String titulo = buscaSaudacao(getNomeUsu(codUsu));
	                //String descricao = "Desejo que seu dia seja produtivo e repleto de conquistas!!! Segue o Líder.";

	                if (countLogin != null && countLogin.compareTo(BigDecimal.valueOf(1)) == 0) {
	                	getMensagemDia(codUsu);
	                	//notifUsu(codUsu, titulo, descricao);
	                }
	            } catch (SQLException e) {
	                e.printStackTrace();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        if (rset != null) {
	            try {
	                rset.close();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	       
	        if (hnd != null) {
	            JapeSession.close(hnd);
	        }
	    }
	}
	
	private void getMensagemDia(BigDecimal codUsu) throws Exception {
	    JdbcWrapper jdbc = null;
	    NativeSql sql = null;
	    ResultSet rset = null;
	    JapeSession.SessionHandle hnd = null;

	    BigDecimal codGrupoUsuLog = getGrupoUsu(codUsu);

	    try {
	        hnd = JapeSession.open();
	        hnd.setFindersMaxRows(-1);
	        final EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	        jdbc = entity.getJdbcWrapper();
	        jdbc.openSession();
	        sql = new NativeSql(jdbc);
	        sql.appendSql("WITH UserList AS (\r\n"
	                + "    SELECT \r\n"
	                + "        MI.ID AS ID,\r\n"
	                + "        MI.CODGRUPO AS CODGRUPO,\r\n"
	                + "        DM.ID_MSG AS ID_MSG,\r\n"
	                + "        DM.DIA AS DIA,\r\n"
	                + "        DM.MENSAGEM AS MENSAGEM,\r\n"
	                + "        DM.ATIVO AS ATIVO,\r\n"
	                + "        DM.DTENVIO AS DTENVIO,\r\n"
	                + "        US.Part AS USUARIO_ID\r\n"
	                + "    FROM \r\n"
	                + "        SANKHYA.AD_MENSAGEMINICIAL MI\r\n"
	                + "    LEFT JOIN \r\n"
	                + "        SANKHYA.AD_DIAMSGINICIAL DM ON DM.ID = MI.ID\r\n"
	                + "    OUTER APPLY \r\n"
	                + "        dbo.SplitString(MI.USUARIOS, ';') AS US\r\n"
	                + "    WHERE \r\n"
	                + "        DM.ATIVO = 'S'\r\n"
	                + "        AND (\r\n"
	                + "            DM.DTENVIO IS NULL AND DM.DIA = CASE \r\n"
	                + "                WHEN DATEPART(dw, GETDATE()) = 1 THEN 7 \r\n"
	                + "                WHEN DATEPART(dw, GETDATE()) = 2 THEN 1  \r\n"
	                + "                WHEN DATEPART(dw, GETDATE()) = 3 THEN 2   \r\n"
	                + "                WHEN DATEPART(dw, GETDATE()) = 4 THEN 3   \r\n"
	                + "                WHEN DATEPART(dw, GETDATE()) = 5 THEN 4  \r\n"
	                + "                WHEN DATEPART(dw, GETDATE()) = 6 THEN 5   \r\n"
	                + "                WHEN DATEPART(dw, GETDATE()) = 7 THEN 6   \r\n"
	                + "                ELSE NULL\r\n"
	                + "            END\r\n"
	                + "            OR \r\n"
	                + "            DM.DTENVIO IS NOT NULL AND CONVERT(DATE, DM.DTENVIO) = CONVERT(DATE, GETDATE()) \r\n"
	                + "        )\r\n"
	                + ")\r\n"
	                + "SELECT \r\n"
	                + "    ID,\r\n"
	                + "    CODGRUPO,\r\n"
	                + "    ID_MSG,\r\n"
	                + "    DIA,\r\n"
	                + "    MENSAGEM,\r\n"
	                + "    ATIVO,\r\n"
	                + "    DTENVIO,\r\n"
	                + "    USUARIO_ID\r\n"
	                + "FROM \r\n"
	                + "    UserList\r\n"
	                + "ORDER BY \r\n"
	                + "    ID;\r\n"
	                + "");

	        rset = sql.executeQuery();

	        boolean mensagemEncontrada = false;

	        while (rset.next()) {
	            try {
	                BigDecimal idRegistro = rset.getBigDecimal("ID");
	                BigDecimal codGrupoMsg = rset.getBigDecimal("CODGRUPO");
	                BigDecimal idDiaMsg = rset.getBigDecimal("ID_MSG");
	                BigDecimal codUsuEnviar = rset.getBigDecimal("USUARIO_ID");

	                String msgDia = rset.getString("MENSAGEM");

	                String titulo = buscaSaudacao(getNomeUsu(codUsu));
	                String descricao = msgDia;

	                if (codUsuEnviar != null && codUsuEnviar.compareTo(codUsu) == 0) {
	                    notifUsu(codUsu, titulo, descricao);
	                    mensagemEncontrada = true;
	                    break;
	                }
	            } catch (SQLException e) {
	                e.printStackTrace();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }

	        // Se não encontrou mensagem específica para o usuário, procurar por grupo
	        if (!mensagemEncontrada) {
	            // Reiniciar o ResultSet
	            JdbcUtils.closeResultSet(rset);
	            sql = new NativeSql(jdbc);
	            sql.appendSql("WITH UserList AS (\r\n"
	                    + "    SELECT \r\n"
	                    + "        MI.ID AS ID,\r\n"
	                    + "        MI.CODGRUPO AS CODGRUPO,\r\n"
	                    + "        DM.ID_MSG AS ID_MSG,\r\n"
	                    + "        DM.DIA AS DIA,\r\n"
	                    + "        DM.MENSAGEM AS MENSAGEM,\r\n"
	                    + "        DM.ATIVO AS ATIVO,\r\n"
	                    + "        DM.DTENVIO AS DTENVIO,\r\n"
	                    + "        US.Part AS USUARIO_ID\r\n"
	                    + "    FROM \r\n"
	                    + "        SANKHYA.AD_MENSAGEMINICIAL MI\r\n"
	                    + "    LEFT JOIN \r\n"
	                    + "        SANKHYA.AD_DIAMSGINICIAL DM ON DM.ID = MI.ID\r\n"
	                    + "    OUTER APPLY \r\n"
	                    + "        dbo.SplitString(MI.USUARIOS, ';') AS US\r\n"
	                    + "    WHERE \r\n"
	                    + "        DM.ATIVO = 'S'\r\n"
	                    + "        AND (\r\n"
	                    + "            DM.DTENVIO IS NULL AND DM.DIA = CASE \r\n"
	                    + "                WHEN DATEPART(dw, GETDATE()) = 1 THEN 7 \r\n"
	                    + "                WHEN DATEPART(dw, GETDATE()) = 2 THEN 1  \r\n"
	                    + "                WHEN DATEPART(dw, GETDATE()) = 3 THEN 2   \r\n"
	                    + "                WHEN DATEPART(dw, GETDATE()) = 4 THEN 3   \r\n"
	                    + "                WHEN DATEPART(dw, GETDATE()) = 5 THEN 4  \r\n"
	                    + "                WHEN DATEPART(dw, GETDATE()) = 6 THEN 5   \r\n"
	                    + "                WHEN DATEPART(dw, GETDATE()) = 7 THEN 6   \r\n"
	                    + "                ELSE NULL\r\n"
	                    + "            END\r\n"
	                    + "            OR \r\n"
	                    + "            DM.DTENVIO IS NOT NULL AND CONVERT(DATE, DM.DTENVIO) = CONVERT(DATE, GETDATE()) \r\n"
	                    + "        )\r\n"
	                    + ")\r\n"
	                    + "SELECT \r\n"
	                    + "    ID,\r\n"
	                    + "    CODGRUPO,\r\n"
	                    + "    ID_MSG,\r\n"
	                    + "    DIA,\r\n"
	                    + "    MENSAGEM,\r\n"
	                    + "    ATIVO,\r\n"
	                    + "    DTENVIO,\r\n"
	                    + "    USUARIO_ID\r\n"
	                    + "FROM \r\n"
	                    + "    UserList\r\n"
	                    + "ORDER BY \r\n"
	                    + "    ID;\r\n"
	                    + "");

	            rset = sql.executeQuery();

	            while (rset.next()) {
	                try {
	                    BigDecimal idRegistro = rset.getBigDecimal("ID");
	                    BigDecimal codGrupoMsg = rset.getBigDecimal("CODGRUPO");
	                    BigDecimal idDiaMsg = rset.getBigDecimal("ID_MSG");
	                    BigDecimal codUsuEnviar = rset.getBigDecimal("USUARIO_ID");

	                    String msgDia = rset.getString("MENSAGEM");

	                    String titulo = buscaSaudacao(getNomeUsu(codUsu));
	                    String descricao = msgDia;

	                    if (codGrupoMsg != null && codGrupoMsg.compareTo(codGrupoUsuLog) == 0) {
	                        notifUsu(codUsu, titulo, descricao);
	                        mensagemEncontrada = true;
	                        break;
	                    }
	                } catch (SQLException e) {
	                    e.printStackTrace();
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        }

	        // Se não encontrou mensagem para o grupo, procurar por codGrupoMsg = 0
	        if (!mensagemEncontrada) {
	            JdbcUtils.closeResultSet(rset);
	            sql = new NativeSql(jdbc);
	            sql.appendSql("SELECT \r\n"
	                    + "    MI.ID AS ID,\r\n"
	                    + "    MI.CODGRUPO AS CODGRUPO,\r\n"
	                    + "    DM.ID_MSG AS ID_MSG,\r\n"
	                    + "    DM.DIA AS DIA,\r\n"
	                    + "    DM.MENSAGEM AS MENSAGEM,\r\n"
	                    + "    DM.ATIVO AS ATIVO,\r\n"
	                    + "    DM.DTENVIO AS DTENVIO\r\n"
	                    + "FROM \r\n"
	                    + "    SANKHYA.AD_MENSAGEMINICIAL MI\r\n"
	                    + "INNER JOIN \r\n"
	                    + "    SANKHYA.AD_DIAMSGINICIAL DM ON DM.ID = MI.ID\r\n"
	                    + "WHERE \r\n"
	                    + "    DM.ATIVO = 'S'\r\n"
	                    + "    AND MI.CODGRUPO = 0\r\n"
	                    + "    AND (\r\n"
	                    + "        DM.DTENVIO IS NULL AND DM.DIA = CASE \r\n"
	                    + "            WHEN DATEPART(dw, GETDATE()) = 1 THEN 7   -- Domingo\r\n"
	                    + "            WHEN DATEPART(dw, GETDATE()) = 2 THEN 1   -- Segunda-feira\r\n"
	                    + "            WHEN DATEPART(dw, GETDATE()) = 3 THEN 2   -- Terça-feira\r\n"
	                    + "            WHEN DATEPART(dw, GETDATE()) = 4 THEN 3   -- Quarta-feira\r\n"
	                    + "            WHEN DATEPART(dw, GETDATE()) = 5 THEN 4   -- Quinta-feira\r\n"
	                    + "            WHEN DATEPART(dw, GETDATE()) = 6 THEN 5   -- Sexta-feira\r\n"
	                    + "            WHEN DATEPART(dw, GETDATE()) = 7 THEN 6   -- Sábado\r\n"
	                    + "            ELSE NULL\r\n"
	                    + "        END\r\n"
	                    + "        OR \r\n"
	                    + "        DM.DTENVIO IS NOT NULL AND CONVERT(DATE, DM.DTENVIO) = CONVERT(DATE, GETDATE()) -- Considera DTENVIO se não for NULL\r\n"
	                    + "    );");

	            rset = sql.executeQuery();

	            while (rset.next()) {
	                try {
	                    BigDecimal idRegistro = rset.getBigDecimal("ID");
	                    BigDecimal idDiaMsg = rset.getBigDecimal("ID_MSG");
	                    String msgDia = rset.getString("MENSAGEM");

	                    String titulo = buscaSaudacao(getNomeUsu(codUsu));
	                    String descricao = msgDia;

	                    notifUsu(codUsu, titulo, descricao);
	                    break;
	                } catch (SQLException e) {
	                    e.printStackTrace();
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        JdbcUtils.closeResultSet(rset);
	        NativeSql.releaseResources(sql);
	        JdbcWrapper.closeSession(jdbc);
	        JapeSession.close(hnd);
	    }
	}

	
	private void notifUsu(BigDecimal codUsu, String titulo, String descricao) throws Exception {
		//salva a imagem na tela NUAVISO com importancia zero(pop-up)
		JapeWrapper avisoDAO = JapeFactory.dao("AvisoSistema");
		DynamicVO avisoVO = (DynamicVO) avisoDAO.create()
				.set("NUAVISO", null)
				.set("CODUSU", codUsu) // DEFINIR USUÁRIO QUE IRÁ RECEBER A NOTIFICAÇÃO
				.set("TITULO", titulo)
				.set("DESCRICAO", descricao)
				.set("DHCRIACAO", TimeUtils.getNow())
				.set("IDENTIFICADOR", "PERSONALIZADO")
				.set("IMPORTANCIA", BigDecimal.ZERO)
				.set("SOLUCAO", null )
				.set("TIPO", "P")
				.save();
	}
	
	public String buscaSaudacao(String usuarioLogadoNome) throws Exception{
        int horaAtual = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        // Determinar a saudação com base na hora atual
        String saudacao = "";
        if (horaAtual >= 0 && horaAtual < 12) {
            saudacao = "Olá, " + usuarioLogadoNome+ ", bom dia!\r\n";
        } else if (horaAtual >= 12 && horaAtual < 18) {
            saudacao = "Olá, " + usuarioLogadoNome + ", boa tarde!\r\n";
        } else {
            saudacao = "Olá, " + usuarioLogadoNome + ", boa noite!\r\n";
        }
        return saudacao;
    }
	

	
	
	public String getNomeUsu(BigDecimal codUsu) {
		JdbcWrapper jdbc = null;
		NativeSql sql = null;
		ResultSet rset = null;
		JapeSession.SessionHandle hnd = null;

		String nomeUsu = null;

		try {
			hnd = JapeSession.open();
			hnd.setFindersMaxRows(-1);
			final EntityFacade entity = EntityFacadeFactory.getDWFFacade();
			jdbc = entity.getJdbcWrapper();
			jdbc.openSession();
			sql = new NativeSql(jdbc);
			sql.appendSql("SELECT NOMEUSUCPLT FROM SANKHYA.TSIUSU WHERE CODUSU = :CODUSU");

			sql.setNamedParameter("CODUSU", codUsu);

			rset = sql.executeQuery();

			if (rset.next()) {
				String nomeCompleto = rset.getString("NOMEUSUCPLT");
				String[] palavras = nomeCompleto.split("\\s+");
				if (palavras.length >= 2) {
	                String primeiraPalavra = palavras[0];
	                String segundaPalavra = palavras[1];  
	                
	                nomeUsu = primeiraPalavra + " " + segundaPalavra;

	            } 
//	            else if (palavras.length == 1) {
//	                String primeiraPalavra = palavras[0];
//	                nomeUsu = primeiraPalavra;
//	            }
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtils.closeResultSet(rset);
			NativeSql.releaseResources(sql);
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}

		return nomeUsu;

	}
	
	public BigDecimal getGrupoUsu(BigDecimal codUsu) {
		JdbcWrapper jdbc = null;
		NativeSql sql = null;
		ResultSet rset = null;
		JapeSession.SessionHandle hnd = null;

		BigDecimal codGrupoUsu = null;

		try {
			hnd = JapeSession.open();
			hnd.setFindersMaxRows(-1);
			final EntityFacade entity = EntityFacadeFactory.getDWFFacade();
			jdbc = entity.getJdbcWrapper();
			jdbc.openSession();
			sql = new NativeSql(jdbc);
			sql.appendSql("SELECT CODGRUPO FROM SANKHYA.TSIUSU WHERE CODUSU = :CODUSU");

			sql.setNamedParameter("CODUSU", codUsu);

			rset = sql.executeQuery();

			if (rset.next()) {
				codGrupoUsu = rset.getBigDecimal("CODGRUPO");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtils.closeResultSet(rset);
			NativeSql.releaseResources(sql);
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}

		return codGrupoUsu;

	}
	

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeInsert(PersistenceEvent arg0) throws Exception {
	
		
		
	}

	@Override
	public void beforeUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
