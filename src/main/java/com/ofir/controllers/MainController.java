package com.ofir.controllers;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.ofir.database.HibernateToDoListDAO;
import com.ofir.database.IToDoListDAO;
import com.ofir.exception.ToDoListDaoException;
import com.ofir.model.Item;
import com.ofir.model.User;

@Controller
@EnableWebMvc
public class MainController {
	
	@RequestMapping("/hello/{name}")
	@ResponseBody
	public String hello(@PathVariable String name){
		return "Hi " + name + "!";
	}
	
	@RequestMapping("/isLoggedFailed")
	@ResponseBody
	public boolean isLoggedFailed(HttpServletRequest request){
		Boolean loginFlag = (Boolean)request.getSession().getAttribute("loggedFailed");
		request.getSession().setAttribute("loggedFailed", null);
		
		if(loginFlag == true){
			return true;
		}
		
		return false;
	}
	
	@RequestMapping("/isRegisterFailed")
	@ResponseBody
	public boolean isRegisterFailed(HttpServletRequest request){
		Boolean loginFlag = (Boolean)request.getSession().getAttribute("registerFailed");
		request.getSession().setAttribute("registerFailed", null);
		
		if(loginFlag == true){
			return true;
		}
		
		return false;
	}
	
	@RequestMapping("/getLoggedUserName")
	@ResponseBody
	public String loggedUserName(HttpServletRequest request){
		User user = (User)request.getSession().getAttribute("user");
		if(user != null){
			return user.getFirstName() + " " + user.getLastName();
		}
		
		return null;
	}
	
	@RequestMapping("/login")
	public String loginPage(){
		return "pages/login.html";
	}
	
	@RequestMapping("/register")
	public String registerPage(){
		return "pages/register.html";
	}
		
	@RequestMapping("/itemsPage")
	public String itemsPage(HttpServletRequest request){
		if(request.getSession().getAttribute("user") != null){
			return "pages/items.html";
		}
		else{
			return "redirect:/*";
		}
	}
	
	@RequestMapping("/logout")
	public String logoutPage(HttpServletRequest request,HttpServletResponse response){
		request.getSession().setAttribute("user", null);
		response.addCookie(new Cookie("userId", null));
		return "redirect:/login";
	}
	
	@RequestMapping(value="/login", method=RequestMethod.POST)
	public String loginAction(HttpServletRequest request,HttpServletResponse response){
		IToDoListDAO DAO = HibernateToDoListDAO.getInstance();
		String email = request.getParameter("email");
		String password = request.getParameter("password");	
		String remember = request.getParameter("remember");
		try {
			User user = DAO.findUser(email, password);
			if(user != null){
				request.getSession().setAttribute("user", user);
				request.getSession().setAttribute("loggedFailed", false);
				if(remember != null){
					response.addCookie(new Cookie("userId",String.valueOf(user.getId())));
				}
				
				return "redirect:/itemsPage";
			}
			else{
				request.getSession().setAttribute("loggedFailed", true);
				return "redirect:/login";
			}
		} catch (ToDoListDaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return "redirect:/open-page";
	}
	
	@RequestMapping(value="/register", method=RequestMethod.POST)
	public String registerAction(HttpServletRequest request){
		IToDoListDAO DAO = HibernateToDoListDAO.getInstance();
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String firstName= request.getParameter("firstName");
		String lastName = request.getParameter("lastName");
		User user = new User(email,firstName,lastName,password);
		try {
			if(user.isUserXssProof()){
				if(DAO.addUser(user))
				{
					request.getSession().setAttribute("registerFailed", false);
					return "redirect:/login";
				}
			}
		} catch (ToDoListDaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		request.getSession().setAttribute("registerFailed", true);
		return "redirect:/register";
	}
	
	@RequestMapping(value="/addItem", method=RequestMethod.POST)
	public String addItem(HttpServletRequest request,@RequestParam("title") String title,@RequestParam("content") String content,@RequestParam("status")String status){
		IToDoListDAO DAO = HibernateToDoListDAO.getInstance();
		User user = (User)request.getSession().getAttribute("user");
		Item item = new Item(title,content,Item.Status.valueOf(status),user.getEmail());
		try {
			if(item.isItemXssProof()){
				DAO.addItem(item);
			}
		} catch (ToDoListDaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "redirect:/itemsPage";
	}
	
	@RequestMapping(value="/editItem", method=RequestMethod.POST)
	@ResponseBody
	public boolean addItemT(HttpServletRequest request,@RequestParam("title") String title,@RequestParam("content") String content,@RequestParam("status")String status,@RequestParam("id") int id){
		IToDoListDAO DAO = HibernateToDoListDAO.getInstance();
		User user = (User)request.getSession().getAttribute("user");
		Item item = new Item(id,title,content,Item.Status.valueOf(status),user.getEmail());
		try {
			if(item.isItemXssProof()){
				DAO.editItem(item);
			}
			return true;
		} catch (ToDoListDaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return  false;
		}
	}
	
	@RequestMapping("/items")
	@ResponseBody
	public String items(HttpServletRequest request){
		IToDoListDAO DAO = HibernateToDoListDAO.getInstance();
		User user = (User)request.getSession().getAttribute("user");
		StringBuilder builder = new StringBuilder();
		try {
			Item[] items = DAO.getItemsOfUser(user);
			for (Item item : items) {
				builder.append(item.toString() + "-");
			}
			builder.deleteCharAt(builder.length()-1);
			return builder.toString();
		} catch (ToDoListDaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	@RequestMapping("/*")
	public String openPage(HttpServletRequest request){
		if(request != null && request.getCookies() != null){
			for (Cookie coockie: request.getCookies()) {
				if(coockie.getName().equals("userId") && coockie.getValue() != null){
					IToDoListDAO DAO = HibernateToDoListDAO.getInstance();
					try {
						User user = DAO.getUser(Integer.valueOf(coockie.getValue()));
						request.getSession().setAttribute("user", user);
	
						return "redirect:/itemsPage";
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ToDoListDaoException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		return  "pages/open-page.html";
	}
}
