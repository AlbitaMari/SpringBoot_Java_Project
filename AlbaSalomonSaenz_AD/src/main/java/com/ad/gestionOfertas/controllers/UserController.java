package com.ad.gestionOfertas.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ad.gestionOfertas.constant.ViewConstant;
import com.ad.gestionOfertas.entities.Ciclos;
import com.ad.gestionOfertas.entities.Inscritos;
import com.ad.gestionOfertas.entities.Ofertas;
import com.ad.gestionOfertas.entities.Usuarios;
import com.ad.gestionOfertas.services.CiclosService;
import com.ad.gestionOfertas.services.InscritosService;
import com.ad.gestionOfertas.services.OfertasService;
import com.ad.gestionOfertas.services.UsuariosService;

@Controller
@RequestMapping("/users")
public class UserController {
	
	private static final Log LOG = LogFactory.getLog(UserController.class);
	
	@Autowired
	private UsuariosService usuariosService;
	
	@Autowired
	private OfertasService ofertasService;
	
	@Autowired
	private CiclosService ciclosService;
	
	@Autowired
	private InscritosService inscritosService;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@GetMapping("/editUser")
	public String userUpdateForm(HttpSession session,Authentication auth,Model model)throws Exception {
		LOG.info("METHOD: editUserForm()");
		String email = auth.getName();
		Usuarios usuario = usuariosService.findUserByEmail(email);
		session.setAttribute("usuario",usuario);
		model.addAttribute("usuario",usuario);
		model.addAttribute("editMode",true);
		
		return ViewConstant.CONTACT_FORM;
	}
	
	@PostMapping("/editUser/edit")
	public String editUser(@Valid @ModelAttribute("usuario") Usuarios usuario,Model model)throws Exception {
		LOG.info("METHOD: editUser() -- PARAMS: " + usuario.toString());
		usuario.setEnabled(true);
		usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
		usuariosService.editUser(usuariosService.transform(usuario));
		return "redirect:/public/index";
	}
	
	@GetMapping("/ofertas")
	public String listOfertas(Model model,Authentication auth, HttpSession session) {
		LOG.info("METHOD: listOfertas()");
		String username = auth.getName();
		Usuarios usuario = usuariosService.findUserByEmail(username);
		session.setAttribute("usuario", usuario);
		model.addAttribute("ofertas",ofertasService.listAllOfertasByRrhh(usuariosService.transform(usuario)));
		return ViewConstant.OFERTAS;
	}

	@GetMapping("/deleteofertas/{id}")
	public String deleteOfertas(Model model, @PathVariable(name="id")int id) throws Exception {
		LOG.info("METHOD: deleteOfertas()");
		ofertasService.deleteOfertas(id);
		return "redirect:/users/ofertas";
		
	}
	
	@GetMapping("/createofertas")
	public ModelAndView createFormOfertastest() {
		LOG.info("METHOD: createOfertasForm()");
		ModelAndView mav = new ModelAndView(ViewConstant.CREAOF);
		mav.addObject("ciclos",ciclosService.listAllCiclos());
		mav.addObject("oferta", new Ofertas());
		return mav;
	}
	
	@PostMapping("/createofertas")
	public String createOferta(@Valid @ModelAttribute Ofertas oferta,Authentication auth, HttpSession session,
			Model model) {
		LOG.info("METHOD: createOferta()");
		String email = auth.getName();
		Usuarios usuario = usuariosService.findUserByEmail(email);
		session.setAttribute("usuario", usuario);
		oferta.setRrhhid(usuario);
		ofertasService.createOferta(ofertasService.transform(oferta));
		return "redirect:/users/ofertas";
	}
	
	@GetMapping("/updateoferta/{id}")
	public String updateOfertaForm(Model model, @PathVariable(name="id")int id) {
		LOG.info("METHOD: updateOfertaForm()");
		Ofertas oferta = ofertasService.findOfertaById(id);
		model.addAttribute("ciclos", ciclosService.listAllCiclos());
		model.addAttribute("oferta",oferta);
		model.addAttribute("editMode",true);
		return ViewConstant.UPDOF;
	}
	
	@PostMapping("/updateoferta/{id}")
	public String updateOfertas(@Valid @ModelAttribute("oferta") Ofertas ofertas,@PathVariable(name="id")int id
			,Authentication auth, HttpSession session) {
		LOG.info("METHOD: updateOfertas()");
		Ofertas oferta = ofertasService.findOfertaById(id);
		String email = auth.getName();
		Usuarios usuario = usuariosService.findUserByEmail(email);
		session.setAttribute("usuario", usuario);
		ofertas.setRrhhid(usuario);
		ofertasService.updateOfertas(ofertasService.transform(ofertas));
		return "redirect:/users/ofertas";
	}
	
	@GetMapping("/showofertas")
	public String listOfertasAlumno(Model model,Authentication auth, HttpSession session) {
		LOG.info("METHOD: listOfertasAlumnos()");
		String email = auth.getName();
		Usuarios usuario = usuariosService.findUserByEmail(email);
		session.setAttribute("usuario", usuario);
		model.addAttribute("ciclos",ciclosService.listAllCiclos());
		model.addAttribute("ofertas",ofertasService.listAllOfertas());
		ArrayList<Integer> inscritosdef = new ArrayList<Integer>();
		List<Inscritos> inscrito = inscritosService.listAllInscritosByAlumnoId(usuariosService.transform(usuario));
		for(int i = 0 ; i < inscrito.size() ; i++) {
			inscritosdef.add(inscrito.get(i).getIdOferta().getId());
		}
		model.addAttribute("inscritos",inscritosdef);
		return ViewConstant.OFERTAS;
	}
	
	@GetMapping("/showofertasfilters")
	public String listOfertasAlFilter(Model model,@RequestParam(name="cicloIds")List<Integer> cicloIds) {
		LOG.info("METHOD: showOfertasFilters()");
		List<Ofertas> ofertas = new ArrayList<Ofertas>();
		for (int i=0;i<cicloIds.size();i++) {
			Ciclos ciclo = ciclosService.findCicloById(cicloIds.get(i));
			List<Ofertas> ofertas_aux = ofertasService.listAllOfertasByCicloId(ciclosService.transform(ciclo));
			for(int j = 0; j < ofertas_aux.size(); j++){
				ofertas.add(ofertas_aux.get(j));
			}
		}
		model.addAttribute("ciclos",ciclosService.listAllCiclos());
		model.addAttribute("ofertas",ofertas);
		return ViewConstant.OFERTAS;
	}
	
	@GetMapping("/inscrib/{id}")
	public String inscribOferta(Model model,Authentication auth, HttpSession session,@PathVariable(name="id")int id) {
		LOG.info("METHOD: inscribOfertas()");
		Ofertas oferta = ofertasService.findOfertaById(id);
		model.addAttribute("ofertas",oferta);
		return ViewConstant.INSCRIB;
	}
	
	@PostMapping("/inscrib/{id}")
	public String inscribirseOferta(Model model,Authentication auth, HttpSession session,@PathVariable(name="id")int id) {
		LOG.info("METHOD: inscribirseOfertas()");
		Ofertas oferta = ofertasService.findOfertaById(id);
		String email = auth.getName();
		Usuarios usuario = usuariosService.findUserByEmail(email);
		session.setAttribute("usuario", usuario);
		Inscritos inscrito = new Inscritos();
		Date fecha = new Date();
		inscrito.setFecha_inscripcion(fecha);
		inscrito.setIdAlumno(usuario);
		inscrito.setIdOferta(oferta);
		ArrayList<Integer> inscritosdef = new ArrayList<Integer>();
		List<Inscritos> inscritos = inscritosService.listAllInscritosByAlumnoId(usuariosService.transform(usuario));
		for(int i = 0 ; i < inscritos.size() ; i++) {
			inscritosdef.add(inscritos.get(i).getIdOferta().getId());
		}
		inscritosService.createInscrito(inscritosService.transform(inscrito));
		model.addAttribute("inscritos",inscritosdef);
		model.addAttribute("ciclos",ciclosService.listAllCiclos());
		model.addAttribute("ofertas",ofertasService.listAllOfertas());
		return ViewConstant.OFERTAS;
	}
	
	@GetMapping("/inscritos")
	public String inscritos(Model model,Authentication auth, HttpSession session) {
		LOG.info("METHOD: listInscritos()");
		String email = auth.getName();
		Usuarios usuario = usuariosService.findUserByEmail(email);
		session.setAttribute("usuario", usuario);
		List<Inscritos> inscrito = inscritosService.listAllInscritosByAlumnoId(usuariosService.transform(usuario));
		model.addAttribute("inscritos",inscrito);
		return ViewConstant.INSCRITOS;
	}
	
	@GetMapping("/ofertasreturn")
	public String returnOfertas(Model model) {
		LOG.info("METHOD: listOfertasAgain()");
		model.addAttribute("ciclos",ciclosService.listAllCiclos());
		model.addAttribute("ofertas",ofertasService.listAllOfertas());
		return ViewConstant.OFERTAS;
	}
	
	@GetMapping("/listusers/{id}")
	public String listUserInOfer(Model model,@PathVariable(name="id")int id) {
		LOG.info("METHOD: listOfertasAgain()");
		Ofertas oferta = ofertasService.findOfertaById(id);
		List<Inscritos> inscritos = inscritosService.listAllInscritosByIdOferta(ofertasService.transform(oferta));
		model.addAttribute("inscritos",inscritos);
		return ViewConstant.INSCRITOS_B;
	}
}
