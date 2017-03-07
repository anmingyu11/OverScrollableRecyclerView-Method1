#BestPullToRefresh

###RecyclerView的适配已经完成，如果适配其他view应该也是大同小异,但是经过阅读android源码发现似乎有更合理的实现方式而不是用动画实现．
--------------
### Todo:
#### 1.仔细研究AbsListView源码，这个在google开发的过程中最初版本是有回弹效果的，结果被苹果告了，然后这个功能便变成了那个overScrollMode的光，丑的一逼．
#### 2.很多原本View中的protected类型的成员变量都被hide掉了，没有办法正常使用，需要仔细研究Scroller.
#### 3.以后会详细整理一下开发者日志，因为有时候连续几个小时工作可能思路不太清晰，写得驴唇不对马嘴．
#### 4.Todo 是todo ，重点还是动手去做．
