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
	
	//thuộc tính defaultValue dùng để gán giá trị nếu ko truyền trên url. Ví dụ: /productList -->page = 1
	//Nếu có truyền trên url. Ví dụ: /productList?page=2 -->page = 2
	@GetMapping(value = {"/productList"})
	public String getAllProductInfos(Model model, @RequestParam(value = "name", defaultValue = "") String likeName,
			@RequestParam(value = "page", defaultValue = "1") int page) {
		System.out.println("Yen FIX");
		final int maxResult = 5;//khai báo số dòng dữ liệu tối đa cho 1 page
		PaginationResult<ProductInfo> productInfos = productDAO.getAllProductInfos(page, maxResult, likeName);

		model.addAttribute("paginationProductInfos", productInfos);
		return "productList";
	}
	
	//HttpServletRequest và HttpServletResponse là 2 đối tượng có sẵn của Servlet, có thể sử dụng hoặc ko sử dụng
	//HttpServletRequest có thể lấy các thông tin của request gửi tới Controller
	//HttpServletResponse có thể trả về thông tin từ Controller cho request gửi tới
	@GetMapping(value = {"/productImage"})
	public void productImage(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("code") String code) throws IOException {
		System.out.println("Dat fix.....");
		Product product = null;
		if (code != null) {
			product = productDAO.getProductByCode(code);
		}

		if (product != null && product.getImage() != null) {
			response.setContentType("image/jpeg, image/jpg, image/png, image/gif");//thay đổi định dạng sẽ trả về
			response.getOutputStream().write(product.getImage());//nội dung sẽ trả về
		}
		response.getOutputStream().close();
	}
	
	//dùng HttpServletRequest để lấy thông tin trong session
	//khi một cartInfo được lấy trong session ra, nó sẽ luôn được đồng bộ trạng thái với session, có nghĩa là khi thay đổi thông tin cartInfo này thì cartInfo tương ứng trong session cũng sẽ bị thay đổi theo
	@GetMapping(value = {"/buyProduct"})
	public String buyProductHandler(HttpServletRequest request, Model model,
			@RequestParam(value = "code", defaultValue = "") String code) {
		Product product = null;

		if (code != null && code.length() > 0) {
			product = productDAO.getProductByCode(code);
		}
		if (product != null) {
			// Thông tin giỏ hàng có thể đã lưu vào trong Session trước đó.
			CartInfo cartInfo = Utils.getCartInfoInSession(request);
			ProductInfo productInfo = new ProductInfo(product);//lấy code,name,price từ product truyền qua ProductInfo
			cartInfo.addProduct(productInfo, 1);
		}

		// Chuyển sang trang danh sách các sản phẩm đã mua.
		return "redirect:/shoppingCart";
	}

	// GET: Hiển thị giỏ hàng.
	@GetMapping(value = {"/shoppingCart"})
	public String shoppingCartHandler(HttpServletRequest request, Model model) {
		System.out.println("Nguyen123 fixed");
		CartInfo cartInfo = Utils.getCartInfoInSession(request);

		model.addAttribute("cartForm", cartInfo);
		return "shoppingCart";
	}
	
	// POST: Cập nhập số lượng cho các sản phẩm đã mua.
	@PostMapping(value = {"/shoppingCart"})
	public String shoppingCartUpdateQuantity(HttpServletRequest request, Model model,
			@ModelAttribute("cartForm") CartInfo cartForm) {
		System.out.println("Tuyen fix.");		
		CartInfo cartInfo = Utils.getCartInfoInSession(request);
		cartInfo.updateQuantity(cartForm);

		// Chuyển sang trang danh sách các sản phẩm đã mua.
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
			// Thông tin giỏ hàng có thể đã lưu vào trong Session trước đó.
			CartInfo cartInfo = Utils.getCartInfoInSession(request);
			ProductInfo productInfo = new ProductInfo(product);
			cartInfo.removeProduct(productInfo);
		}

		// Chuyển sang trang danh sách các sản phẩm đã mua.
		return "redirect:/shoppingCart";
	}

	// GET: Nhập thông tin khách hàng.
	@GetMapping(value = { "/shoppingCartCustomer" })
	public String shoppingCartCustomerForm(HttpServletRequest request, Model model) {
		CartInfo cartInfo = Utils.getCartInfoInSession(request);

		// Chưa mua mặt hàng nào.
		if (cartInfo.isEmpty()) {
			// Chuyển tới trang danh giỏ hàng
			return "redirect:/shoppingCart";
		}

		CustomerInfo customerInfo = cartInfo.getCustomerInfo();
		if (customerInfo == null) {
			customerInfo = new CustomerInfo();
		}

		model.addAttribute("customerForm", customerInfo);
		return "shoppingCartCustomer";
	}
	
	// POST: Save thông tin khách hàng.
	@PostMapping(value = { "/shoppingCartCustomer" })
	public String shoppingCartCustomerSave(HttpServletRequest request, Model model,
			@ModelAttribute("customerForm") @Valid CustomerInfo customerForm, BindingResult result) {
		customerInfoValidator.validate(customerForm, result);
		// Kết quả Validate CustomerInfo.
		if (result.hasErrors()) {
			customerForm.setValid(false);
			return "shoppingCartCustomer";
		}

		customerForm.setValid(true);
		CartInfo cartInfo = Utils.getCartInfoInSession(request);
		cartInfo.setCustomerInfo(customerForm);
		// Chuyển hướng sang trang xác nhận.
		return "redirect:/shoppingCartConfirmation";
	}

	// GET: Xem lại thông tin để xác nhận.
	@GetMapping(value = {"/shoppingCartConfirmation"})
	public String shoppingCartConfirmationReview(HttpServletRequest request, Model model) {
		CartInfo cartInfo = Utils.getCartInfoInSession(request);

		// Chưa mua mặt hàng nào.
		if (cartInfo.isEmpty()) {
			// Chuyển tới trang danh giỏ hàng
			return "redirect:/shoppingCart";
		} else if (!cartInfo.isValidCustomer()) {
			// Chuyển tới trang nhập thông tin khách hàng.
			return "redirect:/shoppingCartCustomer";
		}
		
		return "shoppingCartConfirmation";
	}

	// POST: Gửi đơn hàng (Save).
	@PostMapping(value = {"/shoppingCartConfirmation"})
	public String shoppingCartConfirmationSave(HttpServletRequest request, Model model) {
		CartInfo cartInfo = Utils.getCartInfoInSession(request);

		// Chưa mua mặt hàng nào.
		if (cartInfo.isEmpty()) {
			// Chuyển tới trang danh sách giỏ hàng
			return "redirect:/shoppingCart";
		} else if (!cartInfo.isValidCustomer()) {
			// Chuyển tới trang nhập thông tin khách hàng.
			return "redirect:/shoppingCartCustomer";
		}

		try {
			orderDAO.saveOrder(cartInfo);
		} catch (Exception e) {
			return "shoppingCartConfirmation";
		}

		// Xóa giỏ hàng khỏi session.
		Utils.removeCartInfoInSession(request);

		// Lưu thông tin đơn hàng đã xác nhận mua.
		Utils.storeLastOrderedCartInfoInSession(request, cartInfo);

		// Chuyến hướng tới trang hoàn thành mua hàng.
		return "redirect:/shoppingCartFinalize";
	}
	
	@GetMapping(value = { "/shoppingCartFinalize" })
	public String shoppingCartFinalize(HttpServletRequest request, Model model) {
		CartInfo lastOrderedCart = Utils.getLastOrderedCartInfoInSession(request);
		System.out.println("Son Fixxxxxxx");
		if (lastOrderedCart == null) {
			return "redirect:/shoppingCart";
		}

		return "shoppingCartFinalize";
	}
	

}
