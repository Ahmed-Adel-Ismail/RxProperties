# RxProperties
a library that provides the "Property" concept to Java, in a reactive manner

# Why use RxProperties ?
1- Cleaner code, you do not need to add setters and/or getters to your variables, just Declare a Property and it will handle this for you

2- Properties can observe on each other, where changing one property can cause the other property to be notified and take action, this removes alot of code trying to handle actions and update multiple variables
