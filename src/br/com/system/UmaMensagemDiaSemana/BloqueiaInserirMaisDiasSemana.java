package br.com.system.UmaMensagemDiaSemana;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class BloqueiaInserirMaisDiasSemana implements EventoProgramavelJava {

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
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
	public void beforeInsert(PersistenceEvent event) throws Exception {
	    DynamicVO sepVO = (DynamicVO) event.getVo();

	    String dia = sepVO.asString("DIA"); 
	    BigDecimal idRegistroPrincipal = sepVO.asBigDecimal("ID"); 
	    Timestamp dataProgramada = sepVO.asTimestamp("DTENVIO");
	    
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
            sql.appendSql("SELECT DIA FROM SANKHYA.AD_DIAMSGINICIAL DI\r\n"
                    + "INNER JOIN SANKHYA.AD_MENSAGEMINICIAL MI ON MI.ID = DI.ID\r\n"
                    + "WHERE DI.ID = :ID");
            
            sql.setNamedParameter("ID", idRegistroPrincipal);
            
            rset = sql.executeQuery();
            
            while (rset.next()) {
                String diaRegistrados = rset.getString("DIA");
                if (dia.equals(diaRegistrados)) {
                    throw new Exception("Já existe um registro com o mesmo valor de DIA.");
                }
                if (dataProgramada != null) {
        	        // Converte Timestamp para LocalDate
        	        LocalDate localDate = dataProgramada.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        	        
        	        // Obtém o dia da semana da dataProgramada
        	        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        	        
        	        // Converte DayOfWeek para número (1=Segunda, ..., 7=Domingo)
        	        int diaDaSemana = dayOfWeek.getValue(); // Já está correto (Segunda = 1, ..., Domingo = 7)
        	        
        	        // Compara com o valor de 'dia'
        	        if (!String.valueOf(diaDaSemana).equals(dia)) {
        	            throw new Exception("A data programada deve cair no dia da semana selecionado.");
        	        } 
        	    }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Erro ao verificar duplicidade de DIA.", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;  // Propaga a exceção para que a operação de inserção seja cancelada
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
	
		
	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
	    DynamicVO sepVO = (DynamicVO) event.getVo();
	    DynamicVO old = (DynamicVO) event.getOldVO();

	    // Verificar se o campo DIA foi alterado
	    String diaAtual = sepVO.asString("DIA");
	    String diaAnterior = old.asString("DIA");

	    if (!diaAtual.equals(diaAnterior)) {
	        // O campo DIA foi alterado, então não permitimos a alteração
	        throw new Exception("Alteração no campo 'DIA' não é permitida.");
	    }

	    // Continuar com a lógica existente para outras validações
	    BigDecimal idRegistroPrincipal = sepVO.asBigDecimal("ID");
	    Timestamp dataProgramada = sepVO.asTimestamp("DTENVIO");

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
	        sql.appendSql("SELECT DIA FROM SANKHYA.AD_DIAMSGINICIAL DI\r\n"
	                + "INNER JOIN SANKHYA.AD_MENSAGEMINICIAL MI ON MI.ID = DI.ID\r\n"
	                + "WHERE DI.ID = :ID");

	        sql.setNamedParameter("ID", idRegistroPrincipal);

	        rset = sql.executeQuery();

	        if (rset.next()) {
	            // Valor do campo DIA atual no banco de dados
	            String diaRegistrado = rset.getString("DIA");

	            if (dataProgramada != null) {
        	        // Converte Timestamp para LocalDate
        	        LocalDate localDate = dataProgramada.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        	        
        	        // Obtém o dia da semana da dataProgramada
        	        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        	        
        	        // Converte DayOfWeek para número (1=Segunda, ..., 7=Domingo)
        	        int diaDaSemana = dayOfWeek.getValue(); // Já está correto (Segunda = 1, ..., Domingo = 7)
        	        
        	        // Compara com o valor de 'dia'
        	        if (!String.valueOf(diaDaSemana).equals(diaAtual)) {
        	            throw new Exception("A data programada deve cair no dia da semana selecionado.");
        	        } 
        	    }
	        } else {
	            throw new Exception("Registro não encontrado.");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        throw new Exception("Erro ao verificar duplicidade de DIA.", e);
	    } catch (Exception e) {
	        e.printStackTrace();
	        throw e;  // Propaga a exceção para que a operação de atualização seja cancelada
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

		

}
