package com.umc.pediupatrao.repository;
import com.umc.pediupatrao.entity.Auditoria;
import java.util.List;
public interface AuditoriaRepository extends org.springframework.data.repository.Repository<Auditoria,String> {
 Auditoria insert(Auditoria evento);
 List<Auditoria> findAllByOrderByDataHoraDesc();
}
