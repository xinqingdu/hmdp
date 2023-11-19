## 首页
/shop-type/list  
/blog/hot  
/blog/like/  
/blog/  
## 用户
axios.get("/blog/of/me")
axios.get("/blog/of/follow", {
params: {offset: os, lastId: minTime || new Date().getTime() + 1}
})  
axios.get("/user/me") 
axios.get("/user/info/" + this.user.id)
axios.post("/user/logout")
axios.put("/blog/like/" + b.id)
axios.get("/blog/" + b.id)
## 用户编辑
axios.get("/user/me")
## 登录
axios.post("/user/login", this.form)
axios.post("/user/code?phone="+this.form.phone)
## blog详情
axios.get("/blog/" + id)
axios.get("/shop/" + shopId)
axios.get("/blog/likes/" + id)
axios.put("/blog/like/" +this.blog.id)
axios.get("/follow/or/not/" + this.blog.userId)
axios.put("/follow/" + this.blog.userId + "/" + !this.followed)
axios.get("/user/me")
## blog编辑
axios.get("/shop/of/name?name=" + this.shopName)
axios.post("/blog", data)
axios.post("/upload/blog", formData, config)
axios.get("/upload/blog/delete?name=" + this.fileList[i])
axios.get("/user/me")
## 其他用户
axios.get("/user/info/" + this.user.id)
axios.get("/follow/or/not/" + this.user.id)
axios.get("/follow/common/" + this.user.id)
axios.put("/follow/" + this.user.id + "/" + !this.followed)
## 购物车详情
axios.get("/shop/" + shopId)
axios.get("/voucher/list/" + shopId)
axios.post("/voucher-order/seckill/" + id)
## 购物车列表
axios.get("/shop-type/list")
axios.get("/shop/of/type", { params: this.params })