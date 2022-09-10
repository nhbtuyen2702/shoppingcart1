package com.shoppingcart.model;

import java.util.ArrayList;
import java.util.List;

public class CartInfo {

	private int orderNum;

	private CustomerInfo customerInfo;

	private List<CartLineInfo> cartLineInfos = new ArrayList<CartLineInfo>();

	public void setOrderNum(int orderNum) {
		this.orderNum = orderNum;
	}

	public int getOrderNum() {
		return orderNum;
	}

	public CustomerInfo getCustomerInfo() {
		return customerInfo;
	}
	
	public void setCustomerInfo(CustomerInfo customerInfo) {
		this.customerInfo = customerInfo;
	}

	public List<CartLineInfo> getCartLineInfos() {
		return cartLineInfos;
	}

	//this trong trường hợp này là đối tượng sẽ gọi đến phương thức getCartLineInfoByCode() -->addProduct() -->đối tượng cartInfo trong phương thức buyProductHandler()
	private CartLineInfo getCartLineInfoByCode(String code) {
		for (CartLineInfo cartLineInfo : this.cartLineInfos) {//kiểm tra product đã tồn tại trong giỏ hàng chưa
			if (cartLineInfo.getProductInfo().getCode().equals(code)) {
				return cartLineInfo;
			}
		}
		return null;
	}

	public void addProduct(ProductInfo productInfo, int quantity) {
		CartLineInfo cartLineInfo = getCartLineInfoByCode(productInfo.getCode());

		if (cartLineInfo == null) {
			cartLineInfo = new CartLineInfo();
			cartLineInfo.setQuantity(0);
			cartLineInfo.setProductInfo(productInfo);
			this.cartLineInfos.add(cartLineInfo);
		}

		int newQuantity = cartLineInfo.getQuantity() + quantity;
		if (newQuantity <= 0) {
			this.cartLineInfos.remove(cartLineInfo);
		} else {
			cartLineInfo.setQuantity(newQuantity);
		}
	}

	public void updateQuantity(CartInfo cartForm) {
		if (cartForm != null) {
			List<CartLineInfo> cartLineInfos = cartForm.getCartLineInfos();
			for (CartLineInfo cartLineInfo : cartLineInfos) {
				updateProduct(cartLineInfo.getProductInfo().getCode(), cartLineInfo.getQuantity());
			}
		}
	}

	public void updateProduct(String code, int quantity) {
		CartLineInfo cartLineInfo = getCartLineInfoByCode(code);

		if (cartLineInfo != null) {
			if (quantity <= 0) {
				this.cartLineInfos.remove(cartLineInfo);
			} else {
				cartLineInfo.setQuantity(quantity);
			}
		}
	}

	public void removeProduct(ProductInfo productInfo) {
		CartLineInfo cartLineInfo = getCartLineInfoByCode(productInfo.getCode());
		if (cartLineInfo != null) {
			this.cartLineInfos.remove(cartLineInfo);
		}
	}

	public boolean isEmpty() {
		return this.cartLineInfos.isEmpty();
	}

	public boolean isValidCustomer() {
		return this.customerInfo != null && this.customerInfo.isValid();
	}

	//phương thức getQuantityTotal() sẽ được gọi trong jsp -->${myCartInfo.quantityTotal} 
	//trong jsp muốn gọi phương thức của một đối tượng nào đó thì tên phương thức sẽ là: get + tên khai báo trong jsp(ghi hoa chữ cái đầu)
	//Ví dụ: ${myCartInfo.quantityTotal} -->gọi đến đối tượng cartInfo, tên phương thức getQuantityTotal() 
	public int getQuantityTotal() {
		int quantity = 0;
		for (CartLineInfo cartLineInfo : this.cartLineInfos) {
			quantity += cartLineInfo.getQuantity();
		}
		return quantity;
	}

	//phương thức getAmountTotal() sẽ được gọi trong jsp -->${myCartInfo.amountTotal}
	public double getAmountTotal() {
		double total = 0;
		for (CartLineInfo cartLineInfo : this.cartLineInfos) {
			total += cartLineInfo.getAmount();
		}
		return total;
	}
	
}

