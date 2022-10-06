# QueryUserID

/user/getUid 通过用户hex和id查询用户 但若使用id则会只查询与该id符合的用户  
需要传入参数：Hex或id  

/user/getUser 通过id查询用户，查询时会通过B站API获取符合该UID的用户名，并对数据库进行更新  
需要传入参数：user  

/user/list   通过表名查询用户，使用start和length限定查询的长度  
需要传入参数：tableName  
可传入参数：start,length  

/user/update 通过put请求方法实现对数据的更新，需要校验Key  
需要传入参数:key,id  
可传入参数:hex,name,alias,tableName  

/user/insert 通过POST请求方法实现对数据的更新，需要校验Key  
需要传入参数:id,hex,key  
可传入参数:name,alias,tableName  

/user/createTable 通过get请求方法实现创建表，需要校验Key  
需要传入参数:tableName,key  

/user/getUserForChar 通过get请求方法实现通过弹幕查询用户信息  
需要传入参数:bv号,bChar弹幕内容,timeline弹幕发送时间  

/user/getCharForUser 通过get请求方法实现通过用户查询其对该稿件发送的所有弹幕  
需要传入参数:bv号,User(uid或hex)  

IpUtil类作者：
/**
 * @Author: make mpy
 * @Description: 获取IP的方法
 * @Date: 2021/1/18 15:02
 */
 
 FileUtils类来源：
 https://www.cnblogs.com/xuexidememeda/p/10222212.html
