# FingerDemo
指纹识别


![image](https://github.com/mengcuiguang/FingerDemo/blob/master/test.gif )  

最近在研究android的指纹，因为做的项目是金融类，所有想要把指纹添加到项目中，
但是Android手机有很多种类，有些有指纹，有些没有指纹。这就需要各种判断了。

1、判断当前设备的SDK版本
    因为设备指纹是在android6.0以后才出来的，所以我们首先要判断一下SDK版本是否>=23
    
	if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
	    大于22
	}

2、判断当前设备是否支持指纹
    指纹识别肯定要求设备上有指纹识别的硬件，判断起来也简单
    
         public static boolean isHardWareDetected(Context context) {
		return FingerprintManagerCompat.from(context).isHardwareDetected();
	}
    
3、判断当前设备是否有图案锁
    这个有的一聊，Android在设置指纹时，G爹要求必须要有图案锁 ，可以是password，PIN或者图案都行
    google原生的逻辑就是：想要使用指纹识别的话，必须首先使能屏幕锁才行  
    
    public static boolean isKeyguardSecure(Context context) {
        return ((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardSecure();
    }

4、判断是否设置过指纹

    public static boolean hasEnrolledFingerPrint(Context context) {
        return FingerprintManagerCompat.from(context).hasEnrolledFingerprints();
    }	

根据项目需求，以上设置判断完成以后就可以调用设备指纹了， 调用指纹的方法很简单

authenticate(crypto, flags, cancel, callback, handler)
现在我们挨个解释一下这些参数都是什么：

	1、crypto：这个参数加密类的对象，指纹扫描器会根据它判断认证结果的合法性，如果设置了为Null,那么手机中只要包含了录入的指纹就可以解锁成功，如果放在了金融类里面 你的账户 可以被多个设置过指纹的人使用，太坑爹了。如果不设置为Null的话，内容比较多，后面会讲
	2、cancel：这个是CancellationSignal类的一个对象，这个对象是用来取消指纹操作的，建设不要设置为null，代码有中使用。
	3、flags ：标识位，根据图的文档描述，这个位暂时应该为0，这个标志位应该是保留将来使用的。
	4、callback ：这个就太重要了， 这个是指纹的回调参数，包含了成功，失败，异常等等。
	5、handler:没什么用可以设置为Null


接下来进行细致的分析：

1、CryptoObject对象
	首先，指纹识别的安全性就取决于这个对象 。最不愿看到的是录入指纹的时候被第三方攻击
	FingerprintManager.CryptoObject是基于Java加密API的一个包装类
	FingerprintManager用来保证认证结果的完整性。通常来讲，用来加密指纹扫描结果的机制就是一个Javax.Crypto.Cipher对象。Cipher对象本身会使用由应用调用Android keystore的API产生一个key来实现上面说道的保护功能。
	讲这么多没用的，直接上代码

public class CryptoObjectHelper {
    // This can be key name you want. Should be unique for the app.
    static final String KEY_NAME = "com.meng.android.sample.fingerprint_authentication_key";

    // We always use this keystore on Android.
    static final String KEYSTORE_NAME = "AndroidKeyStore";

    // Should be no need to change these values.
    static final String KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;
    static final String BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC;
    static final String ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7;
    static final String TRANSFORMATION = KEY_ALGORITHM + "/" +
            BLOCK_MODE + "/" +
            ENCRYPTION_PADDING;
    final KeyStore _keystore;

    public CryptoObjectHelper() throws Exception {
        _keystore = KeyStore.getInstance(KEYSTORE_NAME);
        _keystore.load(null);
    }

    public FingerprintManagerCompat.CryptoObject buildCryptoObject() throws Exception {
        Cipher cipher = createCipher(true);
        return new FingerprintManagerCompat.CryptoObject(cipher);
    }

    Cipher createCipher(boolean retry) throws Exception {
        Key key = GetKey();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        try {
            cipher.init(Cipher.ENCRYPT_MODE | Cipher.DECRYPT_MODE, key);
        } catch (KeyPermanentlyInvalidatedException e) {
            _keystore.deleteEntry(KEY_NAME);
            if (retry) {
                createCipher(false);
            } else {
                throw new Exception("Could not create the cipher for fingerprint authentication.", e);
            }
        }
        return cipher;
    }

    Key GetKey() throws Exception {
        Key secretKey;
        if (!_keystore.isKeyEntry(KEY_NAME)) {
            CreateKey();
        }

        secretKey = _keystore.getKey(KEY_NAME, null);
        return secretKey;
    }

    void CreateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM, KEYSTORE_NAME);
        KeyGenParameterSpec keyGenSpec =
                new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(BLOCK_MODE)
                        .setEncryptionPaddings(ENCRYPTION_PADDING)
                        .setUserAuthenticationRequired(true)
                        .build();
        keyGen.init(keyGenSpec);
        keyGen.generateKey();
    }
}

	1、上面的类会针对每个CryptoObject对象都会新建一个Cipher对象，并且会使用由应用生成的key
	2、这个key的名字是使用KEY_NAME变量定义的，这个名字应该是保证唯一的，建议使用APPID区别
	3、GetKey方法会尝试使用Android Keystore的API来解析一个key（名字就是上面我们定义的），如果key不存在的话，那就调用CreateKey方法新建一个key。
	4、cipher变量的实例化是通过调用Cipher.getInstance方法获得的，这个方法接受一个transformation参数，这个参数制定了数据怎么加密和解密。然后调用Cipher.init方法就会使用应用的key来完成cipher对象的实例化工作。

接下来这个Key会在Android中认为无效的地方有：

	1、添加一个新的指纹
	2、当前设备中曾经的指纹不存在了，或全部删除
	3、用户关了屏幕锁功能
	4、用户更换了屏幕锁方法，如password改成PIN或者图案方式
以上的情况Cipher.init方法都会抛出KeyPermanentlyInvalidatedException的异常，上面我的代码中捕获了这个异常，并且删除了当前无效的key，然后根据参数尝试再次创建。


接下来看CreateKey()这个方法：

	1、KeyGenerator类会创建一个key，但是需要一些原始数据才能创建key，这些原始的信息是通过KeyGenParameterSpec类的对象来提供的。
	2、用它的工厂方法getInstance进行的，从上面的代码中我们可以看到这里使用的AES（Advanced Encryption Standard ）加密算法的，AES会将数据分成几个组，然后针对几个组进行加密。

接下来，KeyGenParameterSpec的实例化是使用它的Builder方法:

	1、KEY_NAME：key的名字	
	2、KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT：key必须在加密和解密的时候是有效的
	3、setBlockModes(BLOCK_MODE)：一个被AES切分的数据块都与之前的数据块进行了异或运算了，这样的目的就是为了建立每个数据块之间的依赖关系。
	4、setEncryptionPaddings(ENCRYPTION_PADDING)：这个是上面的常量，表示使用了PKSC7（Public Key Cryptography Standard #7）的方式去产生用于填充AES数据块的字节，这样就是要保证每个数据块的大小是等同的
	5、setUserAuthenticationRequired(true)：调用意味着在使用key之前用户的身份需要被认证。
每次KeyGenParameterSpec创建的时候，他都被用来初始化KeyGenerator，这个对象会产生存储在设备上的key。

最后就是最主要的callback回调了

	1、onAuthenticationError：验证出错回调 指纹传感器会关闭一段时间,在下次调用authenticate时,会出现禁用期(时间依厂商不同30,1分都有)
	这个接口会再系统指纹认证出现不可恢复的错误的时候才会调用，并且参数errorCode就给出了错误码，标识了错误的原因。
	这个时候app能做的只能是提示用户重新尝试一遍。
	2、onAuthenticationHelp：验证帮助回调
	3、onAuthenticationSucceeded：成功回调 
	CryptoObject不是null的话，那么我们在这个方法中可以通过AuthenticationResult来获得Cypher对象然后调用它的doFinal方法。
	doFinal方法会检查结果是不是会拦截或者篡改过，如果是的话会抛出一个异常。
	当我们发现这些异常的时候都应该将认证当做是失败来来处理
	4、onAuthenticationFailed：失败回调

以上内容就是调用Android指纹的内容，总体来说不多。  
这时候是时候抛出一个问题了，如果我们也想向微信那们做一个指纹支付，那是不是就应该是唯一的指纹和密码绑定在一起。
因为不可能是多个指纹都可以用来进行支付，就像上面说到的，一个账户可以被多个指纹进行支付，如果是你朋友不小心用你手机绑了一个指纹，那么。。。
所以我们如果进行指纹和密码绑定，有人说既然设置了指纹就肯定会有指纹Id，用唯一的指纹Id和密码绑定在一起不就好了。
哇，这个想法简单完美，拿到指纹Id这事不就全解决了吗，但是指纹Id真的就这么好拿吗。。

实不相瞒：

	1、AuthenticationResult这个对象是在callback成功回调里拿到的。  这个对象里面就有fingerId但是被@hide修饰了，拿不到了。。。
	2、直接拿不到，我用反射拿不就行了，你会发现就算通过反射来拿Fingerprint对象，拿到的也是null.....
	3、StackOverflow上也有人提出相同的问题：Fingerprint为null，他是直接把android.jar包换了。换成了这里没有@hide修饰了。这样就可以直接在代码里面使用，但是拿到的也是null。
	
	这么多办法就是拿不到怎么办。
	真拿不到啊，我也没有办法。


	这个玩意可以拿到：Tencent/soter   github上面有~~但是还没有研究它是怎么拿到了， 我得继续看了。

















