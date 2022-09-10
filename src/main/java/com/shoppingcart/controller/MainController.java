package com.shoppingcart.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shoppingcart.dao.OrderDAO;
import com.shoppingcart.dao.ProductDAO;
import com.shoppingcart.entity.Product;
import com.shoppingcart.model.CartInfo;
import com.shoppingcart.model.CustomerInfo;
import com.shoppingcart.model.PaginationResult;
import com.shoppingcart.model.ProductInfo;
import com.shoppingcart.util.Utils;
import com.shoppingcart.validator.CustomerInfoValidator;

@Controller
public class MainController {

	@Autowired
	private ProductDAO productDAO;

	@Autowired
	private OrderDAO orderDAO;

	@Autowired
	private CustomerInfoValidator customerInfoValidator;
	
	//thuá»™c tÃ­nh defaultValue dÃ¹ng Ä‘á»ƒ gÃ¡n giÃ¡ trá»‹ náº¿u ko truyá»�n trÃªn url. VÃ­ dá»¥: /productList -->page = 1
	//Náº¿u cÃ³ truyá»�n trÃªn url. VÃ­ dá»¥: /productList?page=2 -->page = 2
	@GetMapping(value = {"/productList"})
	public String getAllProductInfos(Model model, @RequestParam(value = "name", defaultValue = "") String likeName,
			@RequestParam(value = "page", defaultValue = "1") int page) {
		final int maxResult = 5;//khai bÃ¡o sá»‘ dÃ²ng dá»¯ liá»‡u tá»‘i Ä‘a cho 1 page
		PaginationResult<ProductInfo> productInfos = productDAO.getAllProductInfos(page, maxResult, likeName);

		model.addAttribute("paginationProductInfos", productInfos);
		return "productList";
	}
	
	//HttpServletRequest vÃ  HttpServletResponse lÃ  2 Ä‘á»‘i tÆ°á»£ng cÃ³ sáºµn cá»§a Servlet, cÃ³ thá»ƒ sá»­ dá»¥ng hoáº·c ko sá»­ dá»¥ng
	//HttpServletRequest cÃ³ thá»ƒ láº¥y cÃ¡c thÃ´ng tin cá»§a request gá»­i tá»›i Controller
	//HttpServletResponse cÃ³ thá»ƒ tráº£ vá»� thÃ´ng tin tá»« Controller cho request gá»­i tá»›i
	@GetMapping(value = {"/productImage"})
	public void productImage(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("code") String code) throws IOException {
		System.out.println("Dat fix.");
		Product product = null;
		if (code != null) {
			product = productDAO.getProductByCode(code);
		}

		if (product != null && product.getImage() != null) {
			response.setContentType("image/jpeg, image/jpg, image/png, image/gif");//thay Ä‘á»•i Ä‘á»‹nh dáº¡ng sáº½ tráº£ vá»�
			response.getOutputStream().write(product.getImage());//ná»™i dung sáº½ tráº£ vá»�
		}
		response.getOutputStream().close();
	}
	
	//dÃ¹ng HttpServletRequest Ä‘á»ƒ láº¥y thÃ´ng tin trong session
	//khi má»™t cartInfo Ä‘Æ°á»£c láº¥y trong session ra, nÃ³ sáº½ luÃ´n Ä‘Æ°á»£c Ä‘á»“ng bá»™ tráº¡ng thÃ¡i vá»›i session, cÃ³ nghÄ©a lÃ  khi thay Ä‘á»•i thÃ´ng tin cartInfo nÃ y thÃ¬ cartInfo tÆ°Æ¡ng á»©ng trong session cÅ©ng sáº½ bá»‹ thay Ä‘á»•i theo
	@GetMapping(value = {"/buyProduct"})
	public String buyProductHandler(HttpServletRequest request, Model model,
			@RequestParam(value = "code", defaultValue = "") String code) {
		Product product = null;

		if (code != null && code.length() > 0) {
			product = productDAO.getProductByCode(code);
		}
		if (product != null) {
			// ThÃ´ng tin giá»� hÃ ng cÃ³ thá»ƒ Ä‘Ã£ lÆ°u vÃ o trong Session trÆ°á»›c Ä‘Ã³.
			CartInfo cartInfo = Utils.getCartInfoInSession(request);
			ProductInfo productInfo = new ProductInfo(product);//láº¥y code,name,price tá»« product truyá»�n qua ProductInfo
			cartInfo.addProduct(productInfo, 1);
		}

		// Chuyá»ƒn sang trang danh sÃ¡ch cÃ¡c sáº£n pháº©m Ä‘Ã£ mua.
		return "redirect:/shoppingCart";
	}

	// GET: Hiá»ƒn thá»‹ giá»� hÃ ng.
	@GetMapping(value = {"/shoppingCart"})
	public String shoppingCartHandler(HttpServletRequest request, Model model) {
		CartInfo cartInfo = Utils.getCartInfoInSession(request);

		model.addAttribute("cartForm", cartInfo);
		return "shoppingCart";
	}
	
	// POST: Cáº­p nháº­p sá»‘ lÆ°á»£ng cho cÃ¡c sáº£n pháº©m Ä‘Ã£ mua.
	@PostMapping(value = {"/shoppingCart"})
	public String shoppingCartUpdateQuantity(HttpServletRequest request, Model model,
			@ModelAttribute("cartForm") CartInfo cartForm) {
		CartInfo cartInfo = Utils.getCartInfoInSession(request);
		cartInfo.updateQuantity(cartForm);

		// Chuyá»ƒn sang trang danh sÃ¡ch cÃ¡c sáº£n pháº©m Ä‘Ã£ mua.
		return "redirect:/shoppingCart";
	}
	
	@GetMapping(value = {"/shoppingCartRemoveProduct"})
	public String removeProductHandler(HttpServletRequest request, Model model,
			@RequestParam(value = "code", defaultValue = "") String code) {
		Product product = null;

		if (code != null && code.length() > 0) {
			product = productDAO.getProductByCode(code);
		}

		if (product != null) {
			// ThÃ´ng tin giá»� hÃ ng cÃ³ thá»ƒ Ä‘Ã£ lÆ°u vÃ o trong Session trÆ°á»›c Ä‘Ã³.
			CartInfo cartInfo = Utils.getCartInfoInSession(request);
			ProductInfo productInfo = new ProductInfo(product);
			cartInfo.removeProduct(productInfo);
		}

		// Chuyá»ƒn sang trang danh sÃ¡ch cÃ¡c sáº£n pháº©m Ä‘Ã£ mua.
		return "redirect:/shoppingCart";
	}

	// GET: Nháº­p thÃ´ng tin khÃ¡ch hÃ ng.
	@GetMapping(value = { "/shoppingCartCustomer" })
	public String shoppingCartCustomerForm(HttpServletRequest request, Model model) {
		CartInfo cartInfo = Utils.getCartInfoInSession(request);

		// ChÆ°a mua máº·t hÃ ng nÃ o.
		if (cartInfo.isEmpty()) {
			// Chuyá»ƒn tá»›i trang danh giá»� hÃ ng
			return "redirect:/shoppingCart";
		}

		CustomerInfo customerInfo = cartInfo.getCustomerInfo();
		if (customerInfo == null) {
			customerInfo = new CustomerInfo();
		}

		model.addAttribute("customerForm", customerInfo);
		return "shoppingCartCustomer";
	}
	
	// POST: Save thÃ´ng tin khÃ¡ch hÃ ng.
	@PostMapping(value = { "/shoppingCartCustomer" })
	public String shoppingCartCustomerSave(HttpServletRequest request, Model model,
			@ModelAttribute("customerForm") @Valid CustomerInfo customerForm, BindingResult result) {
		customerInfoValidator.validate(customerForm, result);
		// Káº¿t quáº£ Validate CustomerInfo.
		if (result.hasErrors()) {
			customerForm.setValid(false);
			return "shoppingCartCustomer";
		}

		customerForm.setValid(true);
		CartInfo cartInfo = Utils.getCartInfoInSession(request);
		cartInfo.setCustomerInfo(customerForm);
		// Chuyá»ƒn hÆ°á»›ng sang trang xÃ¡c nháº­n.
		return "redirect:/shoppingCartConfirmation";
	}

	// GET: Xem láº¡i thÃ´ng tin Ä‘á»ƒ xÃ¡c nháº­n.
	@GetMapping(value = {"/shoppingCartConfirmation"})
	public String shoppingCartConfirmationReview(HttpServletRequest request, Model model) {
		CartInfo cartInfo = Utils.getCartInfoInSession(request);

		// ChÆ°a mua máº·t hÃ ng nÃ o.
		if (cartInfo.isEmpty()) {
			// Chuyá»ƒn tá»›i trang danh giá»� hÃ ng
			return "redirect:/shoppingCart";
		} else if (!cartInfo.isValidCustomer()) {
			// Chuyá»ƒn tá»›i trang nháº­p thÃ´ng tin khÃ¡ch hÃ ng.
			return "redirect:/shoppingCartCustomer";
		}
		
		return "shoppingCartConfirmation";
	}

	// POST: Gá»­i Ä‘Æ¡n hÃ ng (Save).
	@PostMapping(value = {"/shoppingCartConfirmation"})
	public String shoppingCartConfirmationSave(HttpServletRequest request, Model model) {
		CartInfo cartInfo = Utils.getCartInfoInSession(request);

		// ChÆ°a mua máº·t hÃ ng nÃ o.
		if (cartInfo.isEmpty()) {
			// Chuyá»ƒn tá»›i trang danh sÃ¡ch giá»� hÃ ng
			return "redirect:/shoppingCart";
		} else if (!cartInfo.isValidCustomer()) {
			// Chuyá»ƒn tá»›i trang nháº­p thÃ´ng tin khÃ¡ch hÃ ng.
			return "redirect:/shoppingCartCustomer";
		}

		try {
			orderDAO.saveOrder(cartInfo);
		} catch (Exception e) {
			return "shoppingCartConfirmation";
		}

		// XÃ³a giá»� hÃ ng khá»�i session.
		Utils.removeCartInfoInSession(request);

		// LÆ°u thÃ´ng tin Ä‘Æ¡n hÃ ng Ä‘Ã£ xÃ¡c nháº­n mua.
		Utils.storeLastOrderedCartInfoInSession(request, cartInfo);

		// Chuyáº¿n hÆ°á»›ng tá»›i trang hoÃ n thÃ nh mua hÃ ng.
		return "redirect:/shoppingCartFinalize";
	}
	
	@GetMapping(value = { "/shoppingCartFinalize" })
	public String shoppingCartFinalize(HttpServletRequest request, Model model) {
		CartInfo lastOrderedCart = Utils.getLastOrderedCartInfoInSession(request);

		if (lastOrderedCart == null) {
			return "redirect:/shoppingCart";
		}

		return "shoppingCartFinalize";
	}
	

}
