# CircleImageView
Android 圆形ImageView，可以设置五角星形，可设置边框（border）

项目添加依赖： project/build.gradle中添加：

    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }

project/app/build.gradle中添加：

    dependencies {
        compile 'com.github.luweibin3118:CircleImageView:v1.0.1'
    }


1. 同普通ImageView一样在xml文件中加入布局：

        <com.example.circleimageview.CircleImageView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_margin="10dp"
            android:layout_weight="1"
            android:src="@drawable/test"
            app:border_color="0xff996351"
            app:border_size="50"
            app:corner_number="5"
            app:image_type="0"
            app:smooth_line="true" />

2. 多添加了几种自定义属性：

        <resources>
            <declare-styleable name="circle_view">
                <attr name="image_type" format="integer" />     //ImageView的类型，0：圆形  1：多角星形  2：菱形
                <attr name="corner_number" format="integer" />  //多角星形的角数  eg：五角星设置为5
                <attr name="smooth_line" format="boolean" />    //是否圆滑曲线星形
                <attr name="border_size" format="integer" />    //边框的大小
                <attr name="border_color" format="integer" />   //边框的颜色
            </declare-styleable>
        </resources>

3. 普通属性：

![image](https://github.com/luweibin3118/CircleImageView/blob/master/app/Screenshot_20171228-001316.png)

4. 添加边框：

![image](https://github.com/luweibin3118/CircleImageView/blob/master/app/Screenshot_20171228-001146.png)

5. 普通圆滑：

![image](https://github.com/luweibin3118/CircleImageView/blob/master/app/Screenshot_20171228-001350.png)

6. 添加边框圆滑：

![image](https://github.com/luweibin3118/CircleImageView/blob/master/app/Screenshot_20171228-001433.png)
