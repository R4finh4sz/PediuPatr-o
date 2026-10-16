package com.umc.pediupatrao.controller;
import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.service.AuditoriaService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
public class AuditoriaController {
 private final AuditoriaService auditoria;
 public AuditoriaController(AuditoriaService auditoria) { this.auditoria=auditoria; }
 @GetMapping("/auditoria")
 public String pagina(Model model) {
  model.addAttribute("eventos",auditoria.listar()); model.addAttribute("content","auditoria :: content"); return "layout";
 }
 @GetMapping("/api/auditoria") @ResponseBody
 public List<Auditoria> listar() { return auditoria.listar(); }
}
