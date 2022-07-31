package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.ReferenceKey;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.io.Closeable;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.JSONFactory.*;

public abstract class JSONReader
        implements Closeable {
    static final int MAX_EXP = 512;

    static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    static final ZoneId SHANGHAI_ZONE_ID = DEFAULT_ZONE_ID.getId().equals("Asia/Shanghai") ? DEFAULT_ZONE_ID : ZoneId.of("Asia/Shanghai");
    static final ZoneId UTC = ZoneId.of("UTC");
    static final long LONG_MASK = 0XFFFFFFFFL;

    static final byte JSON_TYPE_INT = 1;
    static final byte JSON_TYPE_DEC = 2;
    static final byte JSON_TYPE_STRING = 3;
    static final byte JSON_TYPE_BOOL = 4;
    static final byte JSON_TYPE_NULL = 5;
    static final byte JSON_TYPE_OBJECT = 6;
    static final byte JSON_TYPE_ARRAY = 7;
    static final byte JSON_TYPE_BIG_DEC = 8;

    static final byte JSON_TYPE_INT8 = 9;
    static final byte JSON_TYPE_INT16 = 10;
    static final byte JSON_TYPE_INT64 = 11;
    static final byte JSON_TYPE_FLOAT = 12;
    static final byte JSON_TYPE_DOUBLE = 13;

    static final char EOI = 0x1A;
    static final long SPACE = (1L << ' ') | (1L << '\n') | (1L << '\r') | (1L << '\f') | (1L << '\t') | (1L << '\b');

    static final float[] FLOAT_SMALL_1 = new float[]{0.0F, 0.1F, 0.2F, 0.3F, 0.4F, 0.5F, 0.6F, 0.7F, 0.8F, 0.9F};
    static final double[] DOUBLE_SMALL_1 = new double[]{0.0D, 0.1D, 0.2D, 0.3D, 0.4D, 0.5D, 0.6D, 0.7D, 0.8D, 0.9D};
    static final float[] FLOAT_SMALL_2 = new float[]{
            0.0F, 0.01F, 0.02F, 0.03F, 0.04F, 0.05F, 0.06F, 0.07F, 0.08F, 0.09F,
            0.1F, 0.11F, 0.12F, 0.13F, 0.14F, 0.15F, 0.16F, 0.17F, 0.18F, 0.19F,
            0.2F, 0.21F, 0.22F, 0.23F, 0.24F, 0.25F, 0.26F, 0.27F, 0.28F, 0.29F,
            0.3F, 0.31F, 0.32F, 0.33F, 0.34F, 0.35F, 0.36F, 0.37F, 0.38F, 0.39F,
            0.4F, 0.41F, 0.42F, 0.43F, 0.44F, 0.45F, 0.46F, 0.47F, 0.48F, 0.49F,
            0.5F, 0.51F, 0.52F, 0.53F, 0.54F, 0.55F, 0.56F, 0.57F, 0.58F, 0.59F,
            0.6F, 0.61F, 0.62F, 0.63F, 0.64F, 0.65F, 0.66F, 0.67F, 0.68F, 0.69F,
            0.7F, 0.71F, 0.72F, 0.73F, 0.74F, 0.75F, 0.76F, 0.77F, 0.78F, 0.79F,
            0.8F, 0.81F, 0.82F, 0.83F, 0.84F, 0.85F, 0.86F, 0.87F, 0.88F, 0.89F,
            0.9F, 0.91F, 0.92F, 0.93F, 0.94F, 0.95F, 0.96F, 0.97F, 0.98F, 0.99F};
    static final double[] DOUBLE_SMALL_2 = new double[]{
            0.0, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09,
            0.1, 0.11, 0.12, 0.13, 0.14, 0.15, 0.16, 0.17, 0.18, 0.19,
            0.2, 0.21, 0.22, 0.23, 0.24, 0.25, 0.26, 0.27, 0.28, 0.29,
            0.3, 0.31, 0.32, 0.33, 0.34, 0.35, 0.36, 0.37, 0.38, 0.39,
            0.4, 0.41, 0.42, 0.43, 0.44, 0.45, 0.46, 0.47, 0.48, 0.49,
            0.5, 0.51, 0.52, 0.53, 0.54, 0.55, 0.56, 0.57, 0.58, 0.59,
            0.6, 0.61, 0.62, 0.63, 0.64, 0.65, 0.66, 0.67, 0.68, 0.69,
            0.7, 0.71, 0.72, 0.73, 0.74, 0.75, 0.76, 0.77, 0.78, 0.79,
            0.8, 0.81, 0.82, 0.83, 0.84, 0.85, 0.86, 0.87, 0.88, 0.89,
            0.9, 0.91, 0.92, 0.93, 0.94, 0.95, 0.96, 0.97, 0.98, 0.99
    };
    static final float[] FLOAT_SMALL_3 = new float[]{
            0.0F, 0.001F, 0.002F, 0.003F, 0.004F, 0.005F, 0.006F, 0.007F, 0.008F, 0.009F, 0.01F, 0.011F, 0.012F, 0.013F, 0.014F, 0.015F, 0.016F, 0.017F, 0.018F, 0.019F, 0.02F, 0.021F, 0.022F, 0.023F, 0.024F,
            0.025F, 0.026F, 0.027F, 0.028F, 0.029F, 0.03F, 0.031F, 0.032F, 0.033F, 0.034F, 0.035F, 0.036F, 0.037F, 0.038F, 0.039F, 0.04F, 0.041F, 0.042F, 0.043F, 0.044F, 0.045F, 0.046F, 0.047F, 0.048F, 0.049F,
            0.05F, 0.051F, 0.052F, 0.053F, 0.054F, 0.055F, 0.056F, 0.057F, 0.058F, 0.059F, 0.06F, 0.061F, 0.062F, 0.063F, 0.064F, 0.065F, 0.066F, 0.067F, 0.068F, 0.069F, 0.07F, 0.071F, 0.072F, 0.073F, 0.074F,
            0.075F, 0.076F, 0.077F, 0.078F, 0.079F, 0.08F, 0.081F, 0.082F, 0.083F, 0.084F, 0.085F, 0.086F, 0.087F, 0.088F, 0.089F, 0.09F, 0.091F, 0.092F, 0.093F, 0.094F, 0.095F, 0.096F, 0.097F, 0.098F, 0.099F,
            0.1F, 0.101F, 0.102F, 0.103F, 0.104F, 0.105F, 0.106F, 0.107F, 0.108F, 0.109F, 0.11F, 0.111F, 0.112F, 0.113F, 0.114F, 0.115F, 0.116F, 0.117F, 0.118F, 0.119F, 0.12F, 0.121F, 0.122F, 0.123F, 0.124F,
            0.125F, 0.126F, 0.127F, 0.128F, 0.129F, 0.13F, 0.131F, 0.132F, 0.133F, 0.134F, 0.135F, 0.136F, 0.137F, 0.138F, 0.139F, 0.14F, 0.141F, 0.142F, 0.143F, 0.144F, 0.145F, 0.146F, 0.147F, 0.148F, 0.149F,
            0.15F, 0.151F, 0.152F, 0.153F, 0.154F, 0.155F, 0.156F, 0.157F, 0.158F, 0.159F, 0.16F, 0.161F, 0.162F, 0.163F, 0.164F, 0.165F, 0.166F, 0.167F, 0.168F, 0.169F, 0.17F, 0.171F, 0.172F, 0.173F, 0.174F,
            0.175F, 0.176F, 0.177F, 0.178F, 0.179F, 0.18F, 0.181F, 0.182F, 0.183F, 0.184F, 0.185F, 0.186F, 0.187F, 0.188F, 0.189F, 0.19F, 0.191F, 0.192F, 0.193F, 0.194F, 0.195F, 0.196F, 0.197F, 0.198F, 0.199F,
            0.2F, 0.201F, 0.202F, 0.203F, 0.204F, 0.205F, 0.206F, 0.207F, 0.208F, 0.209F, 0.21F, 0.211F, 0.212F, 0.213F, 0.214F, 0.215F, 0.216F, 0.217F, 0.218F, 0.219F, 0.22F, 0.221F, 0.222F, 0.223F, 0.224F,
            0.225F, 0.226F, 0.227F, 0.228F, 0.229F, 0.23F, 0.231F, 0.232F, 0.233F, 0.234F, 0.235F, 0.236F, 0.237F, 0.238F, 0.239F, 0.24F, 0.241F, 0.242F, 0.243F, 0.244F, 0.245F, 0.246F, 0.247F, 0.248F, 0.249F,
            0.25F, 0.251F, 0.252F, 0.253F, 0.254F, 0.255F, 0.256F, 0.257F, 0.258F, 0.259F, 0.26F, 0.261F, 0.262F, 0.263F, 0.264F, 0.265F, 0.266F, 0.267F, 0.268F, 0.269F, 0.27F, 0.271F, 0.272F, 0.273F, 0.274F,
            0.275F, 0.276F, 0.277F, 0.278F, 0.279F, 0.28F, 0.281F, 0.282F, 0.283F, 0.284F, 0.285F, 0.286F, 0.287F, 0.288F, 0.289F, 0.29F, 0.291F, 0.292F, 0.293F, 0.294F, 0.295F, 0.296F, 0.297F, 0.298F, 0.299F,
            0.3F, 0.301F, 0.302F, 0.303F, 0.304F, 0.305F, 0.306F, 0.307F, 0.308F, 0.309F, 0.31F, 0.311F, 0.312F, 0.313F, 0.314F, 0.315F, 0.316F, 0.317F, 0.318F, 0.319F, 0.32F, 0.321F, 0.322F, 0.323F, 0.324F,
            0.325F, 0.326F, 0.327F, 0.328F, 0.329F, 0.33F, 0.331F, 0.332F, 0.333F, 0.334F, 0.335F, 0.336F, 0.337F, 0.338F, 0.339F, 0.34F, 0.341F, 0.342F, 0.343F, 0.344F, 0.345F, 0.346F, 0.347F, 0.348F, 0.349F,
            0.35F, 0.351F, 0.352F, 0.353F, 0.354F, 0.355F, 0.356F, 0.357F, 0.358F, 0.359F, 0.36F, 0.361F, 0.362F, 0.363F, 0.364F, 0.365F, 0.366F, 0.367F, 0.368F, 0.369F, 0.37F, 0.371F, 0.372F, 0.373F, 0.374F,
            0.375F, 0.376F, 0.377F, 0.378F, 0.379F, 0.38F, 0.381F, 0.382F, 0.383F, 0.384F, 0.385F, 0.386F, 0.387F, 0.388F, 0.389F, 0.39F, 0.391F, 0.392F, 0.393F, 0.394F, 0.395F, 0.396F, 0.397F, 0.398F, 0.399F,
            0.4F, 0.401F, 0.402F, 0.403F, 0.404F, 0.405F, 0.406F, 0.407F, 0.408F, 0.409F, 0.41F, 0.411F, 0.412F, 0.413F, 0.414F, 0.415F, 0.416F, 0.417F, 0.418F, 0.419F, 0.42F, 0.421F, 0.422F, 0.423F, 0.424F,
            0.425F, 0.426F, 0.427F, 0.428F, 0.429F, 0.43F, 0.431F, 0.432F, 0.433F, 0.434F, 0.435F, 0.436F, 0.437F, 0.438F, 0.439F, 0.44F, 0.441F, 0.442F, 0.443F, 0.444F, 0.445F, 0.446F, 0.447F, 0.448F, 0.449F,
            0.45F, 0.451F, 0.452F, 0.453F, 0.454F, 0.455F, 0.456F, 0.457F, 0.458F, 0.459F, 0.46F, 0.461F, 0.462F, 0.463F, 0.464F, 0.465F, 0.466F, 0.467F, 0.468F, 0.469F, 0.47F, 0.471F, 0.472F, 0.473F, 0.474F,
            0.475F, 0.476F, 0.477F, 0.478F, 0.479F, 0.48F, 0.481F, 0.482F, 0.483F, 0.484F, 0.485F, 0.486F, 0.487F, 0.488F, 0.489F, 0.49F, 0.491F, 0.492F, 0.493F, 0.494F, 0.495F, 0.496F, 0.497F, 0.498F, 0.499F,
            0.5F, 0.501F, 0.502F, 0.503F, 0.504F, 0.505F, 0.506F, 0.507F, 0.508F, 0.509F, 0.51F, 0.511F, 0.512F, 0.513F, 0.514F, 0.515F, 0.516F, 0.517F, 0.518F, 0.519F, 0.52F, 0.521F, 0.522F, 0.523F, 0.524F,
            0.525F, 0.526F, 0.527F, 0.528F, 0.529F, 0.53F, 0.531F, 0.532F, 0.533F, 0.534F, 0.535F, 0.536F, 0.537F, 0.538F, 0.539F, 0.54F, 0.541F, 0.542F, 0.543F, 0.544F, 0.545F, 0.546F, 0.547F, 0.548F, 0.549F,
            0.55F, 0.551F, 0.552F, 0.553F, 0.554F, 0.555F, 0.556F, 0.557F, 0.558F, 0.559F, 0.56F, 0.561F, 0.562F, 0.563F, 0.564F, 0.565F, 0.566F, 0.567F, 0.568F, 0.569F, 0.57F, 0.571F, 0.572F, 0.573F, 0.574F,
            0.575F, 0.576F, 0.577F, 0.578F, 0.579F, 0.58F, 0.581F, 0.582F, 0.583F, 0.584F, 0.585F, 0.586F, 0.587F, 0.588F, 0.589F, 0.59F, 0.591F, 0.592F, 0.593F, 0.594F, 0.595F, 0.596F, 0.597F, 0.598F, 0.599F,
            0.6F, 0.601F, 0.602F, 0.603F, 0.604F, 0.605F, 0.606F, 0.607F, 0.608F, 0.609F, 0.61F, 0.611F, 0.612F, 0.613F, 0.614F, 0.615F, 0.616F, 0.617F, 0.618F, 0.619F, 0.62F, 0.621F, 0.622F, 0.623F, 0.624F,
            0.625F, 0.626F, 0.627F, 0.628F, 0.629F, 0.63F, 0.631F, 0.632F, 0.633F, 0.634F, 0.635F, 0.636F, 0.637F, 0.638F, 0.639F, 0.64F, 0.641F, 0.642F, 0.643F, 0.644F, 0.645F, 0.646F, 0.647F, 0.648F, 0.649F,
            0.65F, 0.651F, 0.652F, 0.653F, 0.654F, 0.655F, 0.656F, 0.657F, 0.658F, 0.659F, 0.66F, 0.661F, 0.662F, 0.663F, 0.664F, 0.665F, 0.666F, 0.667F, 0.668F, 0.669F, 0.67F, 0.671F, 0.672F, 0.673F, 0.674F,
            0.675F, 0.676F, 0.677F, 0.678F, 0.679F, 0.68F, 0.681F, 0.682F, 0.683F, 0.684F, 0.685F, 0.686F, 0.687F, 0.688F, 0.689F, 0.69F, 0.691F, 0.692F, 0.693F, 0.694F, 0.695F, 0.696F, 0.697F, 0.698F, 0.699F,
            0.7F, 0.701F, 0.702F, 0.703F, 0.704F, 0.705F, 0.706F, 0.707F, 0.708F, 0.709F, 0.71F, 0.711F, 0.712F, 0.713F, 0.714F, 0.715F, 0.716F, 0.717F, 0.718F, 0.719F, 0.72F, 0.721F, 0.722F, 0.723F, 0.724F,
            0.725F, 0.726F, 0.727F, 0.728F, 0.729F, 0.73F, 0.731F, 0.732F, 0.733F, 0.734F, 0.735F, 0.736F, 0.737F, 0.738F, 0.739F, 0.74F, 0.741F, 0.742F, 0.743F, 0.744F, 0.745F, 0.746F, 0.747F, 0.748F, 0.749F,
            0.75F, 0.751F, 0.752F, 0.753F, 0.754F, 0.755F, 0.756F, 0.757F, 0.758F, 0.759F, 0.76F, 0.761F, 0.762F, 0.763F, 0.764F, 0.765F, 0.766F, 0.767F, 0.768F, 0.769F, 0.77F, 0.771F, 0.772F, 0.773F, 0.774F,
            0.775F, 0.776F, 0.777F, 0.778F, 0.779F, 0.78F, 0.781F, 0.782F, 0.783F, 0.784F, 0.785F, 0.786F, 0.787F, 0.788F, 0.789F, 0.79F, 0.791F, 0.792F, 0.793F, 0.794F, 0.795F, 0.796F, 0.797F, 0.798F, 0.799F,
            0.8F, 0.801F, 0.802F, 0.803F, 0.804F, 0.805F, 0.806F, 0.807F, 0.808F, 0.809F, 0.81F, 0.811F, 0.812F, 0.813F, 0.814F, 0.815F, 0.816F, 0.817F, 0.818F, 0.819F, 0.82F, 0.821F, 0.822F, 0.823F, 0.824F,
            0.825F, 0.826F, 0.827F, 0.828F, 0.829F, 0.83F, 0.831F, 0.832F, 0.833F, 0.834F, 0.835F, 0.836F, 0.837F, 0.838F, 0.839F, 0.84F, 0.841F, 0.842F, 0.843F, 0.844F, 0.845F, 0.846F, 0.847F, 0.848F, 0.849F,
            0.85F, 0.851F, 0.852F, 0.853F, 0.854F, 0.855F, 0.856F, 0.857F, 0.858F, 0.859F, 0.86F, 0.861F, 0.862F, 0.863F, 0.864F, 0.865F, 0.866F, 0.867F, 0.868F, 0.869F, 0.87F, 0.871F, 0.872F, 0.873F, 0.874F,
            0.875F, 0.876F, 0.877F, 0.878F, 0.879F, 0.88F, 0.881F, 0.882F, 0.883F, 0.884F, 0.885F, 0.886F, 0.887F, 0.888F, 0.889F, 0.89F, 0.891F, 0.892F, 0.893F, 0.894F, 0.895F, 0.896F, 0.897F, 0.898F, 0.899F,
            0.9F, 0.901F, 0.902F, 0.903F, 0.904F, 0.905F, 0.906F, 0.907F, 0.908F, 0.909F, 0.91F, 0.911F, 0.912F, 0.913F, 0.914F, 0.915F, 0.916F, 0.917F, 0.918F, 0.919F, 0.92F, 0.921F, 0.922F, 0.923F, 0.924F,
            0.925F, 0.926F, 0.927F, 0.928F, 0.929F, 0.93F, 0.931F, 0.932F, 0.933F, 0.934F, 0.935F, 0.936F, 0.937F, 0.938F, 0.939F, 0.94F, 0.941F, 0.942F, 0.943F, 0.944F, 0.945F, 0.946F, 0.947F, 0.948F, 0.949F,
            0.95F, 0.951F, 0.952F, 0.953F, 0.954F, 0.955F, 0.956F, 0.957F, 0.958F, 0.959F, 0.96F, 0.961F, 0.962F, 0.963F, 0.964F, 0.965F, 0.966F, 0.967F, 0.968F, 0.969F, 0.97F, 0.971F, 0.972F, 0.973F, 0.974F,
            0.975F, 0.976F, 0.977F, 0.978F, 0.979F, 0.98F, 0.981F, 0.982F, 0.983F, 0.984F, 0.985F, 0.986F, 0.987F, 0.988F, 0.989F, 0.99F, 0.991F, 0.992F, 0.993F, 0.994F, 0.995F, 0.996F, 0.997F, 0.998F, 0.999F
    };

    static final double[] DOUBLE_SMALL_3 = new double[]{
            0.0, 0.001, 0.002, 0.003, 0.004, 0.005, 0.006, 0.007, 0.008, 0.009, 0.01, 0.011, 0.012, 0.013, 0.014, 0.015, 0.016, 0.017, 0.018, 0.019, 0.02, 0.021, 0.022, 0.023, 0.024,
            0.025, 0.026, 0.027, 0.028, 0.029, 0.03, 0.031, 0.032, 0.033, 0.034, 0.035, 0.036, 0.037, 0.038, 0.039, 0.04, 0.041, 0.042, 0.043, 0.044, 0.045, 0.046, 0.047, 0.048, 0.049,
            0.05, 0.051, 0.052, 0.053, 0.054, 0.055, 0.056, 0.057, 0.058, 0.059, 0.06, 0.061, 0.062, 0.063, 0.064, 0.065, 0.066, 0.067, 0.068, 0.069, 0.07, 0.071, 0.072, 0.073, 0.074,
            0.075, 0.076, 0.077, 0.078, 0.079, 0.08, 0.081, 0.082, 0.083, 0.084, 0.085, 0.086, 0.087, 0.088, 0.089, 0.09, 0.091, 0.092, 0.093, 0.094, 0.095, 0.096, 0.097, 0.098, 0.099,
            0.1, 0.101, 0.102, 0.103, 0.104, 0.105, 0.106, 0.107, 0.108, 0.109, 0.11, 0.111, 0.112, 0.113, 0.114, 0.115, 0.116, 0.117, 0.118, 0.119, 0.12, 0.121, 0.122, 0.123, 0.124,
            0.125, 0.126, 0.127, 0.128, 0.129, 0.13, 0.131, 0.132, 0.133, 0.134, 0.135, 0.136, 0.137, 0.138, 0.139, 0.14, 0.141, 0.142, 0.143, 0.144, 0.145, 0.146, 0.147, 0.148, 0.149,
            0.15, 0.151, 0.152, 0.153, 0.154, 0.155, 0.156, 0.157, 0.158, 0.159, 0.16, 0.161, 0.162, 0.163, 0.164, 0.165, 0.166, 0.167, 0.168, 0.169, 0.17, 0.171, 0.172, 0.173, 0.174,
            0.175, 0.176, 0.177, 0.178, 0.179, 0.18, 0.181, 0.182, 0.183, 0.184, 0.185, 0.186, 0.187, 0.188, 0.189, 0.19, 0.191, 0.192, 0.193, 0.194, 0.195, 0.196, 0.197, 0.198, 0.199,
            0.2, 0.201, 0.202, 0.203, 0.204, 0.205, 0.206, 0.207, 0.208, 0.209, 0.21, 0.211, 0.212, 0.213, 0.214, 0.215, 0.216, 0.217, 0.218, 0.219, 0.22, 0.221, 0.222, 0.223, 0.224,
            0.225, 0.226, 0.227, 0.228, 0.229, 0.23, 0.231, 0.232, 0.233, 0.234, 0.235, 0.236, 0.237, 0.238, 0.239, 0.24, 0.241, 0.242, 0.243, 0.244, 0.245, 0.246, 0.247, 0.248, 0.249,
            0.25, 0.251, 0.252, 0.253, 0.254, 0.255, 0.256, 0.257, 0.258, 0.259, 0.26, 0.261, 0.262, 0.263, 0.264, 0.265, 0.266, 0.267, 0.268, 0.269, 0.27, 0.271, 0.272, 0.273, 0.274,
            0.275, 0.276, 0.277, 0.278, 0.279, 0.28, 0.281, 0.282, 0.283, 0.284, 0.285, 0.286, 0.287, 0.288, 0.289, 0.29, 0.291, 0.292, 0.293, 0.294, 0.295, 0.296, 0.297, 0.298, 0.299,
            0.3, 0.301, 0.302, 0.303, 0.304, 0.305, 0.306, 0.307, 0.308, 0.309, 0.31, 0.311, 0.312, 0.313, 0.314, 0.315, 0.316, 0.317, 0.318, 0.319, 0.32, 0.321, 0.322, 0.323, 0.324,
            0.325, 0.326, 0.327, 0.328, 0.329, 0.33, 0.331, 0.332, 0.333, 0.334, 0.335, 0.336, 0.337, 0.338, 0.339, 0.34, 0.341, 0.342, 0.343, 0.344, 0.345, 0.346, 0.347, 0.348, 0.349,
            0.35, 0.351, 0.352, 0.353, 0.354, 0.355, 0.356, 0.357, 0.358, 0.359, 0.36, 0.361, 0.362, 0.363, 0.364, 0.365, 0.366, 0.367, 0.368, 0.369, 0.37, 0.371, 0.372, 0.373, 0.374,
            0.375, 0.376, 0.377, 0.378, 0.379, 0.38, 0.381, 0.382, 0.383, 0.384, 0.385, 0.386, 0.387, 0.388, 0.389, 0.39, 0.391, 0.392, 0.393, 0.394, 0.395, 0.396, 0.397, 0.398, 0.399,
            0.4, 0.401, 0.402, 0.403, 0.404, 0.405, 0.406, 0.407, 0.408, 0.409, 0.41, 0.411, 0.412, 0.413, 0.414, 0.415, 0.416, 0.417, 0.418, 0.419, 0.42, 0.421, 0.422, 0.423, 0.424,
            0.425, 0.426, 0.427, 0.428, 0.429, 0.43, 0.431, 0.432, 0.433, 0.434, 0.435, 0.436, 0.437, 0.438, 0.439, 0.44, 0.441, 0.442, 0.443, 0.444, 0.445, 0.446, 0.447, 0.448, 0.449,
            0.45, 0.451, 0.452, 0.453, 0.454, 0.455, 0.456, 0.457, 0.458, 0.459, 0.46, 0.461, 0.462, 0.463, 0.464, 0.465, 0.466, 0.467, 0.468, 0.469, 0.47, 0.471, 0.472, 0.473, 0.474,
            0.475, 0.476, 0.477, 0.478, 0.479, 0.48, 0.481, 0.482, 0.483, 0.484, 0.485, 0.486, 0.487, 0.488, 0.489, 0.49, 0.491, 0.492, 0.493, 0.494, 0.495, 0.496, 0.497, 0.498, 0.499,
            0.5, 0.501, 0.502, 0.503, 0.504, 0.505, 0.506, 0.507, 0.508, 0.509, 0.51, 0.511, 0.512, 0.513, 0.514, 0.515, 0.516, 0.517, 0.518, 0.519, 0.52, 0.521, 0.522, 0.523, 0.524,
            0.525, 0.526, 0.527, 0.528, 0.529, 0.53, 0.531, 0.532, 0.533, 0.534, 0.535, 0.536, 0.537, 0.538, 0.539, 0.54, 0.541, 0.542, 0.543, 0.544, 0.545, 0.546, 0.547, 0.548, 0.549,
            0.55, 0.551, 0.552, 0.553, 0.554, 0.555, 0.556, 0.557, 0.558, 0.559, 0.56, 0.561, 0.562, 0.563, 0.564, 0.565, 0.566, 0.567, 0.568, 0.569, 0.57, 0.571, 0.572, 0.573, 0.574,
            0.575, 0.576, 0.577, 0.578, 0.579, 0.58, 0.581, 0.582, 0.583, 0.584, 0.585, 0.586, 0.587, 0.588, 0.589, 0.59, 0.591, 0.592, 0.593, 0.594, 0.595, 0.596, 0.597, 0.598, 0.599,
            0.6, 0.601, 0.602, 0.603, 0.604, 0.605, 0.606, 0.607, 0.608, 0.609, 0.61, 0.611, 0.612, 0.613, 0.614, 0.615, 0.616, 0.617, 0.618, 0.619, 0.62, 0.621, 0.622, 0.623, 0.624,
            0.625, 0.626, 0.627, 0.628, 0.629, 0.63, 0.631, 0.632, 0.633, 0.634, 0.635, 0.636, 0.637, 0.638, 0.639, 0.64, 0.641, 0.642, 0.643, 0.644, 0.645, 0.646, 0.647, 0.648, 0.649,
            0.65, 0.651, 0.652, 0.653, 0.654, 0.655, 0.656, 0.657, 0.658, 0.659, 0.66, 0.661, 0.662, 0.663, 0.664, 0.665, 0.666, 0.667, 0.668, 0.669, 0.67, 0.671, 0.672, 0.673, 0.674,
            0.675, 0.676, 0.677, 0.678, 0.679, 0.68, 0.681, 0.682, 0.683, 0.684, 0.685, 0.686, 0.687, 0.688, 0.689, 0.69, 0.691, 0.692, 0.693, 0.694, 0.695, 0.696, 0.697, 0.698, 0.699,
            0.7, 0.701, 0.702, 0.703, 0.704, 0.705, 0.706, 0.707, 0.708, 0.709, 0.71, 0.711, 0.712, 0.713, 0.714, 0.715, 0.716, 0.717, 0.718, 0.719, 0.72, 0.721, 0.722, 0.723, 0.724,
            0.725, 0.726, 0.727, 0.728, 0.729, 0.73, 0.731, 0.732, 0.733, 0.734, 0.735, 0.736, 0.737, 0.738, 0.739, 0.74, 0.741, 0.742, 0.743, 0.744, 0.745, 0.746, 0.747, 0.748, 0.749,
            0.75, 0.751, 0.752, 0.753, 0.754, 0.755, 0.756, 0.757, 0.758, 0.759, 0.76, 0.761, 0.762, 0.763, 0.764, 0.765, 0.766, 0.767, 0.768, 0.769, 0.77, 0.771, 0.772, 0.773, 0.774,
            0.775, 0.776, 0.777, 0.778, 0.779, 0.78, 0.781, 0.782, 0.783, 0.784, 0.785, 0.786, 0.787, 0.788, 0.789, 0.79, 0.791, 0.792, 0.793, 0.794, 0.795, 0.796, 0.797, 0.798, 0.799,
            0.8, 0.801, 0.802, 0.803, 0.804, 0.805, 0.806, 0.807, 0.808, 0.809, 0.81, 0.811, 0.812, 0.813, 0.814, 0.815, 0.816, 0.817, 0.818, 0.819, 0.82, 0.821, 0.822, 0.823, 0.824,
            0.825, 0.826, 0.827, 0.828, 0.829, 0.83, 0.831, 0.832, 0.833, 0.834, 0.835, 0.836, 0.837, 0.838, 0.839, 0.84, 0.841, 0.842, 0.843, 0.844, 0.845, 0.846, 0.847, 0.848, 0.849,
            0.85, 0.851, 0.852, 0.853, 0.854, 0.855, 0.856, 0.857, 0.858, 0.859, 0.86, 0.861, 0.862, 0.863, 0.864, 0.865, 0.866, 0.867, 0.868, 0.869, 0.87, 0.871, 0.872, 0.873, 0.874,
            0.875, 0.876, 0.877, 0.878, 0.879, 0.88, 0.881, 0.882, 0.883, 0.884, 0.885, 0.886, 0.887, 0.888, 0.889, 0.89, 0.891, 0.892, 0.893, 0.894, 0.895, 0.896, 0.897, 0.898, 0.899,
            0.9, 0.901, 0.902, 0.903, 0.904, 0.905, 0.906, 0.907, 0.908, 0.909, 0.91, 0.911, 0.912, 0.913, 0.914, 0.915, 0.916, 0.917, 0.918, 0.919, 0.92, 0.921, 0.922, 0.923, 0.924,
            0.925, 0.926, 0.927, 0.928, 0.929, 0.93, 0.931, 0.932, 0.933, 0.934, 0.935, 0.936, 0.937, 0.938, 0.939, 0.94, 0.941, 0.942, 0.943, 0.944, 0.945, 0.946, 0.947, 0.948, 0.949,
            0.95, 0.951, 0.952, 0.953, 0.954, 0.955, 0.956, 0.957, 0.958, 0.959, 0.96, 0.961, 0.962, 0.963, 0.964, 0.965, 0.966, 0.967, 0.968, 0.969, 0.97, 0.971, 0.972, 0.973, 0.974,
            0.975, 0.976, 0.977, 0.978, 0.979, 0.98, 0.981, 0.982, 0.983, 0.984, 0.985, 0.986, 0.987, 0.988, 0.989, 0.99, 0.991, 0.992, 0.993, 0.994, 0.995, 0.996, 0.997, 0.998, 0.999
    };

    static final double[] DOUBLES_4 = new double[]{
            0.0000D, 0.0001D, 0.0002D, 0.0003D, 0.0004D, 0.0005D, 0.0006D, 0.0007D, 0.0008D, 0.0009D
    };

    static final double[] DOUBLES_5 = new double[]{
            0.00000D, 0.00001D, 0.00002D, 0.00003D, 0.00004D, 0.00005D, 0.00006D, 0.00007D, 0.00008D, 0.00009D,
            0.00010D, 0.00011D, 0.00012D, 0.00013D, 0.00014D, 0.00015D, 0.00016D, 0.00017D, 0.00018D, 0.00019D,
            0.00020D, 0.00021D, 0.00022D, 0.00023D, 0.00024D, 0.00025D, 0.00026D, 0.00027D, 0.00028D, 0.00029D,
            0.00030D, 0.00031D, 0.00032D, 0.00033D, 0.00034D, 0.00035D, 0.00036D, 0.00037D, 0.00038D, 0.00039D,
            0.00040D, 0.00041D, 0.00042D, 0.00043D, 0.00044D, 0.00045D, 0.00046D, 0.00047D, 0.00048D, 0.00049D,
            0.00050D, 0.00051D, 0.00052D, 0.00053D, 0.00054D, 0.00055D, 0.00056D, 0.00057D, 0.00058D, 0.00059D,
            0.00060D, 0.00061D, 0.00062D, 0.00063D, 0.00064D, 0.00065D, 0.00066D, 0.00067D, 0.00068D, 0.00069D,
            0.00070D, 0.00071D, 0.00072D, 0.00073D, 0.00074D, 0.00075D, 0.00076D, 0.00077D, 0.00078D, 0.00079D,
            0.00080D, 0.00081D, 0.00082D, 0.00083D, 0.00084D, 0.00085D, 0.00086D, 0.00087D, 0.00088D, 0.00089D,
            0.00090D, 0.00091D, 0.00092D, 0.00093D, 0.00094D, 0.00095D, 0.00096D, 0.00097D, 0.00098D, 0.00099D,
    };

    final Context context;
    List<ResolveTask> resolveTasks;

    protected int offset;
    protected char ch;
    protected boolean comma;

    protected boolean nameEscape;
    protected boolean valueEscape;
    protected boolean wasNull;
    protected boolean boolValue;
    protected boolean negative;

    protected byte valueType;
    protected byte exponent;
    protected byte scale;

    protected int mag0;
    protected int mag1;
    protected int mag2;
    protected int mag3;

    protected String stringValue;
    protected Object complex; // Map | List

    protected boolean typeRedirect; // redirect for {"@type":"xxx"",...

    public final char current() {
        return ch;
    }

    public final boolean isEnd() {
        return ch == EOI;
    }

    public byte getType() {
        return -128;
    }

    public boolean isInt() {
        return ch == '-' || ch == '+' || (ch >= '0' && ch <= '9');
    }

    public abstract boolean isNull();

    public abstract Date readNullOrNewDate();

    public abstract boolean nextIfNull();

    public JSONReader(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public boolean isEnabled(Feature feature) {
        return (context.features & feature.mask) != 0;
    }

    public Locale getLocale() {
        return context.getLocale();
    }

    public ZoneId getZoneId() {
        return context.getZoneId();
    }

    public long features(long features) {
        return context.features | features;
    }

    public void handleResolveTasks(Object root) {
        if (resolveTasks == null) {
            return;
        }

        Object previous = null;
        for (ResolveTask resolveTask : resolveTasks) {
            JSONPath path = resolveTask.reference;
            FieldReader fieldReader = resolveTask.fieldReader;

            Object fieldValue;
            if (path.isPrevious()) {
                fieldValue = previous;
            } else {
                if (!path.isRef()) {
                    throw new JSONException("reference path invalid : " + path);
                }
                path.setReaderContext(context);
                if ((context.features & Feature.FieldBased.mask) != 0) {
                    JSONWriter.Context writeContext = JSONFactory.createWriteContext();
                    writeContext.features |= JSONWriter.Feature.FieldBased.mask;
                    path.setWriterContext(writeContext);
                }

                fieldValue = path.eval(root);
                previous = fieldValue;
            }

            Object resolvedName = resolveTask.name;
            Object resolvedObject = resolveTask.object;

            if (resolvedName != null) {
                if (resolvedObject instanceof Map) {
                    Map map = (Map) resolvedObject;
                    if (resolvedName instanceof ReferenceKey) {
                        if (map instanceof LinkedHashMap) {
                            int size = map.size();
                            if (size == 0) {
                                continue;
                            }

                            Object[] keys = new Object[size];
                            Object[] values = new Object[size];

                            int index = 0;
                            for (Object o : map.entrySet()) {
                                Map.Entry entry = (Map.Entry) o;
                                Object entryKey = entry.getKey();
                                if (resolvedName == entryKey) {
                                    keys[index] = fieldValue;
                                } else {
                                    keys[index] = entryKey;
                                }
                                values[index++] = entry.getValue();
                            }
                            map.clear();

                            for (int j = 0; j < keys.length; j++) {
                                map.put(keys[j], values[j]);
                            }
                        } else {
                            map.put(fieldValue, map.remove(resolvedName));
                        }
                    } else {
                        map.put(resolvedName, fieldValue);
                    }
                    continue;
                }

                if (resolvedName instanceof Integer) {
                    if (resolvedObject instanceof List) {
                        int index = (Integer) resolvedName;
                        List list = (List) resolvedObject;
                        list.set(index, fieldValue);
                        continue;
                    }

                    if (resolvedObject instanceof Object[]) {
                        int index = (Integer) resolvedName;
                        Object[] array = (Object[]) resolvedObject;
                        array[index] = fieldValue;
                        continue;
                    }

                    if (resolvedObject instanceof Collection) {
                        Collection collection = (Collection) resolvedObject;
                        collection.add(fieldValue);
                        continue;
                    }
                }
            }

            fieldReader.accept(resolvedObject, fieldValue);
        }
    }

    public ObjectReader getObjectReader(Type type) {
        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        return context.provider.getObjectReader(type, fieldBased);
    }

    public boolean isSupportSmartMatch() {
        return (context.features & Feature.SupportSmartMatch.mask) != 0;
    }

    public boolean isSupportSmartMatch(long features) {
        return ((context.features | features) & Feature.SupportSmartMatch.mask) != 0;
    }

    public boolean isSupportBeanArray() {
        return (context.features & Feature.SupportArrayToBean.mask) != 0;
    }

    public boolean isSupportBeanArray(long features) {
        return ((context.features | features) & Feature.SupportArrayToBean.mask) != 0;
    }

    public boolean isSupportAutoType(long features) {
        return ((context.features | features) & Feature.SupportAutoType.mask) != 0;
    }

    public boolean isJSONB() {
        return false;
    }

    public boolean isIgnoreNoneSerializable() {
        return (context.features & Feature.IgnoreNoneSerializable.mask) != 0;
    }

    public ObjectReader checkAutoType(Class expectClass, long expectClassHash, long features) {
        return null;
    }

    static char char1(int c) {
        switch (c) {
            case '0':
                return '\0';
            case '1':
                return '\1';
            case '2':
                return '\2';
            case '3':
                return '\3';
            case '4':
                return '\4';
            case '5':
                return '\5';
            case '6':
                return '\6';
            case '7':
                return '\7';
            case 'b': // 8
                return '\b';
            case 't': // 9
                return '\t';
            case 'n': // 10
                return '\n';
            case 'v': // 11
                return '\u000B';
            case 'f': // 12
            case 'F':
                return '\f';
            case 'r': // 13
                return '\r';
            case '"': // 34
            case '\'': // 39
            case '/': // 47
            case '.': // 47
            case '\\': // 92
            case '#':
            case '&':
            case '[':
            case ']':
            case '@':
            case '(':
            case ')':
                return (char) c;
            default:
                throw new JSONException("unclosed.str.lit " + (char) c);
        }
    }

    static char char2(int c1, int c2) {
        return (char) (DIGITS2[c1] * 0x10
                + DIGITS2[c2]);
    }

    static char char4(int c1, int c2, int c3, int c4) {
        return (char) (DIGITS2[c1] * 0x1000
                + DIGITS2[c2] * 0x100
                + DIGITS2[c3] * 0x10
                + DIGITS2[c4]);
    }

    public boolean nextIfObjectStart() {
        if (this.ch != '{') {
            return false;
        }
        next();
        return true;
    }

    public abstract boolean nextIfEmptyString();

    public boolean nextIfObjectEnd() {
        if (this.ch != '}') {
            return false;
        }
        next();
        return true;
    }

    public int startArray() {
        next();
        return 0;
    }

    public abstract boolean isReference();

    public abstract String readReference();

    public void addResolveTask(FieldReader fieldReader, Object object, JSONPath path) {
        if (resolveTasks == null) {
            resolveTasks = new ArrayList<>();
        }
        resolveTasks.add(new ResolveTask(fieldReader, object, fieldReader.getFieldName(), path));
    }

    public void addResolveTask(Map object, Object key, JSONPath reference) {
        if (resolveTasks == null) {
            resolveTasks = new ArrayList<>();
        }
        if (object instanceof LinkedHashMap) {
            object.put(key, null);
        }
        resolveTasks.add(new ResolveTask(null, object, key, reference));
    }

    public void addResolveTask(Collection object, int i, JSONPath reference) {
        if (resolveTasks == null) {
            resolveTasks = new ArrayList<>();
        }
        resolveTasks.add(new ResolveTask(null, object, i, reference));
    }

    public void addResolveTask(Object[] object, int i, JSONPath reference) {
        if (resolveTasks == null) {
            resolveTasks = new ArrayList<>();
        }
        resolveTasks.add(new ResolveTask(null, object, i, reference));
    }

    public boolean isArray() {
        return this.ch == '[';
    }

    public boolean isObject() {
        return this.ch == '{';
    }

    public boolean isNumber() {
        switch (this.ch) {
            case '-':
            case '+':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return true;
            default:
                return false;
        }
    }

    public boolean isString() {
        return this.ch == '"' || this.ch == '\'';
    }

    public void endArray() {
        next();
    }

    public abstract boolean nextIfMatch(char ch);

    public abstract boolean nextIfSet();

    public abstract String readPattern();

    public final int getOffset() {
        return offset;
    }

    public abstract void next();

    public abstract long readValueHashCode();

    public long readTypeHashCode() {
        return readValueHashCode();
    }

    public abstract long readFieldNameHashCode();

    public abstract long getNameHashCodeLCase();

    public abstract String readFieldName();

    public abstract String getFieldName();

    public void setTypeRedirect(boolean typeRedirect) {
        this.typeRedirect = typeRedirect;
    }

    public boolean isTypeRedirect() {
        return typeRedirect;
    }

    public abstract long readFieldNameHashCodeUnquote();

    public String readFieldNameUnquote() {
        readFieldNameHashCodeUnquote();
        return getFieldName();
    }

    public abstract boolean skipName();

    public abstract void skipValue();

    public boolean isBinary() {
        return false;
    }

    public byte[] readBinary() {
        if (isString()) {
            String str = readString();
            if (str.isEmpty()) {
                return null;
            }

            throw new JSONException(info("not support input " + str));
        }

        if (nextIfMatch('[')) {
            int index = 0;
            byte[] bytes = new byte[64];
            while (true) {
                if (ch == ']') {
                    next();
                    break;
                }
                if (index == bytes.length) {
                    int oldCapacity = bytes.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    bytes = Arrays.copyOf(bytes, newCapacity);
                }
                bytes[index++] = (byte) readInt32Value();
            }
            nextIfMatch(',');
            return Arrays.copyOf(bytes, index);
        }

        throw new JSONException(info("not support read binary"));
    }

    public abstract int readInt32Value();

    public boolean nextIfMatch(byte type) {
        throw new JSONException("UnsupportedOperation");
    }

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2);

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2, char c3);

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4, char c5);

    public abstract Integer readInt32();

    public int getInt32Value() {
        switch (valueType) {
            case JSON_TYPE_INT:
                if (mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                    return negative ? -mag3 : mag3;
                }
                return getNumber().intValue();
            case JSON_TYPE_DEC:
                return getNumber().intValue();
            case JSON_TYPE_BOOL:
                return boolValue ? 1 : 0;
            case JSON_TYPE_NULL:
                return 0;
            case JSON_TYPE_STRING: {
                return toInt32(stringValue);
            }
            case JSON_TYPE_OBJECT: {
                Number num = toNumber((Map) complex);
                if (num != null) {
                    return num.intValue();
                }
                return 0;
            }
            case JSON_TYPE_ARRAY: {
                return toInt((List) complex);
            }
            default:
                throw new JSONException("TODO : " + valueType);
        }
    }

    protected long getInt64Value() {
        switch (valueType) {
            case JSON_TYPE_DEC:
                return getNumber().longValue();
            case JSON_TYPE_BOOL:
                return boolValue ? 1 : 0;
            case JSON_TYPE_NULL:
                return 0;
            case JSON_TYPE_STRING: {
                return toInt64(stringValue);
            }
            case JSON_TYPE_OBJECT: {
                return toLong((Map) complex);
            }
            case JSON_TYPE_ARRAY: {
                return toInt((List) complex);
            }
            default:
                throw new JSONException("TODO");
        }
    }

    protected Long getInt64() {
        switch (valueType) {
            case JSON_TYPE_INT:
                if (mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                    return Long.valueOf(negative ? -mag3 : mag3);
                }
                int[] mag;
                if (mag0 == 0) {
                    if (mag1 == 0) {
                        if (mag2 == Integer.MIN_VALUE && mag3 == 0 && !negative) {
                            return Long.MIN_VALUE;
                        }

                        long v3 = mag3 & LONG_MASK;
                        long v2 = mag2 & LONG_MASK;

                        if (v2 >= Integer.MIN_VALUE && v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            return negative ? -v23 : v23;
                        }
                        mag = new int[]{mag2, mag3};
                    } else {
                        mag = new int[]{mag1, mag2, mag3};
                    }
                } else {
                    mag = new int[]{mag0, mag1, mag2, mag3};
                }

                return getBigInt(negative, mag).longValue();
            case JSON_TYPE_DEC:
                return getNumber().longValue();
            case JSON_TYPE_BOOL:
                return Long.valueOf(boolValue ? 1 : 0);
            case JSON_TYPE_NULL:
                return null;
            case JSON_TYPE_STRING: {
                return toInt64(stringValue);
            }
            case JSON_TYPE_OBJECT: {
                Number num = toNumber((Map) complex);
                if (num != null) {
                    return num.longValue();
                }
                return null;
            }
            default:
                throw new JSONException("TODO");
        }
    }

    public abstract long readInt64Value();

    public abstract Long readInt64();

    public float readFloatValue() {
        readNumber0();

        if (wasNull) {
            return 0;
        }

        switch (valueType) {
            case JSON_TYPE_INT:
            case JSON_TYPE_INT64: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                    int intVlaue;
                    if (negative) {
                        if (mag3 < 0) {
                            return -(mag3 & 0xFFFFFFFFL);
                        }
                        intVlaue = -mag3;
                    } else {
                        if (mag3 < 0) {
                            return mag3 & 0xFFFFFFFFL;
                        }
                        intVlaue = mag3;
                    }

                    return (float) intVlaue;
                }
                int[] mag;
                if (mag0 == 0) {
                    if (mag1 == 0) {
                        long v3 = mag3 & LONG_MASK;
                        long v2 = mag2 & LONG_MASK;

                        if (v2 >= Integer.MIN_VALUE && v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            return negative ? -v23 : v23;
                        }
                        mag = new int[]{mag2, mag3};
                    } else {
                        mag = new int[]{mag1, mag2, mag3};
                    }
                } else {
                    mag = new int[]{mag0, mag1, mag2, mag3};
                }

                return getBigInt(negative, mag).floatValue();
            }
            case JSON_TYPE_INT16: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 >= 0) {
                    int intValue = negative ? -mag3 : mag3;
                    return (float) intValue;
                }
                break;
            }
            case JSON_TYPE_INT8: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 >= 0) {
                    int intValue = negative ? -mag3 : mag3;
                    return (float) intValue;
                }
                break;
            }
            case JSON_TYPE_DEC: {
                if (exponent == 0 && mag0 == 0 && mag1 == 0) {
                    if (mag2 == 0 && mag3 >= 0) {
                        int unscaledVal = negative ? -mag3 : mag3;
                        if (scale == 1) {
                            int small = unscaledVal % 10;
                            return unscaledVal / 10 + FLOAT_SMALL_1[small];
                        } else if (scale == 2) {
                            int small = unscaledVal % 100;
                            return unscaledVal / 100 + FLOAT_SMALL_2[small];
                        } else if (scale == 3) {
                            int small = unscaledVal % 1000;
                            return unscaledVal / 1000 + FLOAT_SMALL_3[small];
                        } else if (scale == 4) {
                            return floatValue4(unscaledVal);
                        } else if (scale == 5) {
                            return floatValue5(unscaledVal);
                        }
                    } else {
                        long v3 = mag3 & LONG_MASK;
                        long v2 = mag2 & LONG_MASK;

                        if (v2 >= Integer.MIN_VALUE && v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            long unscaledVal = negative ? -v23 : v23;
                            if (scale == 1) {
                                int small = (int) (unscaledVal % 10);
                                return unscaledVal / 10 + FLOAT_SMALL_1[small];
                            } else if (scale == 2) {
                                int small = (int) (unscaledVal % 100);
                                return unscaledVal / 100 + FLOAT_SMALL_2[small];
                            } else if (scale == 3) {
                                int small = (int) (unscaledVal % 1000);
                                return unscaledVal / 1000 + FLOAT_SMALL_3[small];
                            } else if (scale == 4) {
                                return floatValue4(unscaledVal);
                            } else if (scale == 5) {
                                return floatValue5(unscaledVal);
                            }
                        }
                    }
                }

                int[] mag = mag0 == 0
                        ? mag1 == 0
                        ? mag2 == 0
                        ? new int[]{mag3}
                        : new int[]{mag2, mag3}
                        : new int[]{mag1, mag2, mag3}
                        : new int[]{mag0, mag1, mag2, mag3};
                BigInteger bigInt = getBigInt(negative, mag);
                BigDecimal decimal = new BigDecimal(bigInt, scale);

                if (exponent != 0) {
                    return Float.parseFloat(
                            decimal + "E" + exponent);
                }

                return decimal.floatValue();
            }
            default:
                break;
        }

        Number number = getNumber();
        return number == null ? 0 : number.floatValue();
    }

    static float floatValue4(long unscaledVal) {
        int smallValue = (int) (unscaledVal % 10000);

        double d3 = DOUBLE_SMALL_3[smallValue / 10];
        double d4 = DOUBLES_4[smallValue % 10];
        float floatSmallValue = (float) (d3 + d4);

        return unscaledVal / 10000 + floatSmallValue;
    }

    static float floatValue5(long unscaledVal) {
        int smallValue = (int) (unscaledVal % 100000);

        double d3 = DOUBLE_SMALL_3[smallValue / 100];
        double d5 = DOUBLES_5[smallValue % 100];
        float floatSmallValue = (float) (d3 + d5);

        return unscaledVal / 100000 + floatSmallValue;
    }

    public Float readFloat() {
        float value = readFloatValue();
        if (value == 0 && wasNull) {
            return null;
        }
        return value;
    }

    public double readDoubleValue() {
        readNumber0();

        if (wasNull) {
            return 0;
        }

        switch (valueType) {
            case JSON_TYPE_INT:
            case JSON_TYPE_INT64: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                    int intVlaue;
                    if (negative) {
                        if (mag3 < 0) {
                            return -(mag3 & 0xFFFFFFFFL);
                        }
                        intVlaue = -mag3;
                    } else {
                        if (mag3 < 0) {
                            return mag3 & 0xFFFFFFFFL;
                        }
                        intVlaue = mag3;
                    }

                    return intVlaue;
                }
                int[] mag;
                if (mag0 == 0) {
                    if (mag1 == 0) {
                        long v3 = mag3 & LONG_MASK;
                        long v2 = mag2 & LONG_MASK;

                        if (v2 >= Integer.MIN_VALUE && v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            return negative ? -v23 : v23;
                        }
                        mag = new int[]{mag2, mag3};
                    } else {
                        mag = new int[]{mag1, mag2, mag3};
                    }
                } else {
                    mag = new int[]{mag0, mag1, mag2, mag3};
                }

                return getBigInt(negative, mag).floatValue();
            }
            case JSON_TYPE_INT16: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 >= 0) {
                    int intValue = negative ? -mag3 : mag3;
                    return intValue;
                }
                break;
            }
            case JSON_TYPE_INT8: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 >= 0) {
                    int intValue = negative ? -mag3 : mag3;
                    return intValue;
                }
                break;
            }
            case JSON_TYPE_DEC: {
                BigDecimal decimal = null;

                if (exponent == 0 && mag0 == 0 && mag1 == 0) {
                    if (mag2 == 0 && mag3 >= 0) {
                        int unscaledVal = negative ? -mag3 : mag3;
                        if (scale == 1) {
                            int small = unscaledVal % 10;
                            return unscaledVal / 10 + DOUBLE_SMALL_1[small];
                        } else if (scale == 2) {
                            int small = unscaledVal % 100;
                            return unscaledVal / 100 + DOUBLE_SMALL_2[small];
                        } else if (scale == 3) {
                            int small = unscaledVal % 1000;
                            return unscaledVal / 1000 + DOUBLE_SMALL_3[small];
                        }
                    } else {
                        long v3 = mag3 & LONG_MASK;
                        long v2 = mag2 & LONG_MASK;

                        if (v2 >= Integer.MIN_VALUE && v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            long unscaledVal = negative ? -v23 : v23;
                            if (scale == 1) {
                                int small = (int) (unscaledVal % 10);
                                return unscaledVal / 10 + DOUBLE_SMALL_1[small];
                            } else if (scale == 2) {
                                int small = (int) (unscaledVal % 100);
                                return unscaledVal / 100 + DOUBLE_SMALL_2[small];
                            } else if (scale == 3) {
                                int small = (int) (unscaledVal % 1000);
                                return unscaledVal / 1000 + DOUBLE_SMALL_3[small];
                            }
                        }
                    }
                }

                if (decimal == null) {
                    int[] mag = mag0 == 0
                            ? mag1 == 0
                            ? mag2 == 0
                            ? new int[]{mag3}
                            : new int[]{mag2, mag3}
                            : new int[]{mag1, mag2, mag3}
                            : new int[]{mag0, mag1, mag2, mag3};
                    BigInteger bigInt = getBigInt(negative, mag);
                    decimal = new BigDecimal(bigInt, scale);
                }

                if (exponent != 0) {
                    return Double.parseDouble(
                            decimal + "E" + exponent);
                }

                return decimal.doubleValue();
            }
            default:
                break;
        }

        Number number = getNumber();
        return number == null ? 0 : number.doubleValue();
    }

    public Double readDouble() {
        double value = readDoubleValue();
        if (value == 0 && wasNull) {
            return null;
        }
        return value;
    }

    public Number readNumber() {
        readNumber0();
        return getNumber();
    }

    public BigInteger readBigInteger() {
        readNumber0();
        return getBigInteger();
    }

    public BigDecimal readBigDecimal() {
        readNumber0();
        return getBigDecimal();
    }

    public abstract UUID readUUID();

    public boolean isLocalDate() {
        if (!isString()) {
            return false;
        }

        LocalDateTime localDateTime;
        int len = getStringLength();
        switch (len) {
            case 8:
                localDateTime = readLocalDate8();
                break;
            case 9:
                localDateTime = readLocalDate9();
                break;
            case 10:
                localDateTime = readLocalDate10();
                break;
            case 11:
                localDateTime = readLocalDate11();
                break;
            default:
                return false;
        }

        if (localDateTime == null) {
            return false;
        }
        return localDateTime.getHour() == 0
                && localDateTime.getMinute() == 0
                && localDateTime.getSecond() == 0
                && localDateTime.getNano() == 0;
    }

    public LocalDate readLocalDate() {
        if (nextIfNull()) {
            return null;
        }

        if (isInt()) {
            long millis = readInt64Value();
            if (context.formatUnixTime) {
                millis *= 1000L;
            }
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = instant.atZone(context.getZoneId());
            return zdt.toLocalDate();
        }

        if (context.dateFormat == null
                || context.formatyyyyMMddhhmmss19
                || context.formatyyyyMMddhhmmssT19
                || context.formatyyyyMMdd8
                || context.formatISO8601) {
            int len = getStringLength();
            LocalDateTime ldt = null;
            switch (len) {
                case 8:
                    ldt = readLocalDate8();
                    break;
                case 9:
                    ldt = readLocalDate9();
                    break;
                case 10:
                    ldt = readLocalDate10();
                    break;
                case 11:
                    ldt = readLocalDate11();
                    break;
                case 19:
                    ldt = readLocalDateTime19();
                    break;
                default:
                    if (len > 20) {
                        ldt = readLocalDateTimeX(len);
                    }
                    break;
            }
            if (ldt != null) {
                return ldt.toLocalDate();
            }
        }

        String str = readString();
        if (str.isEmpty() || "null".equals(str)) {
            return null;
        }

        DateTimeFormatter formatter = context.getDateFormatter();
        if (formatter != null) {
            if (context.formatHasHour) {
                return LocalDateTime
                        .parse(str, formatter)
                        .toLocalDate();
            }
            return LocalDate.parse(str, formatter);
        }

        if (IOUtils.isNumber(str)) {
            long millis = Long.parseLong(str);
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = instant.atZone(context.getZoneId());
            return zdt.toLocalDate();
        }

        throw new JSONException("not support input : " + str);
    }

    public boolean isLocalDateTime() {
        if (!isString()) {
            return false;
        }

        int len = getStringLength();
        switch (len) {
            case 16:
                return readLocalDateTime16() != null;
            case 17:
                return readLocalDateTime17() != null;
            case 18:
                return readLocalDateTime18() != null;
            case 19:
                return readLocalDateTime19() != null;
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
                return readLocalDateTimeX(len) != null;
            default:
                break;
        }
        return false;
    }

    public LocalDateTime readLocalDateTime() {
        if (isInt()) {
            long millis = readInt64Value();
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = instant.atZone(context.getZoneId());
            return zdt.toLocalDateTime();
        }

        if (context.dateFormat == null
                || context.formatyyyyMMddhhmmss19
                || context.formatyyyyMMddhhmmssT19
                || context.formatyyyyMMdd8
                || context.formatISO8601) {
            int len = getStringLength();
            switch (len) {
                case 8:
                    return readLocalDate8();
                case 9:
                    return readLocalDate9();
                case 10:
                    return readLocalDate10();
                case 11:
                    return readLocalDate11();
                case 16:
                    return readLocalDateTime16();
                case 17:
                    return readLocalDateTime17();
                case 18:
                    return readLocalDateTime18();
                case 19:
                    return readLocalDateTime19();
                case 20:
                    return readZonedDateTimeX(len)
                            .toLocalDateTime();
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                    LocalDateTime ldt = readLocalDateTimeX(len);
                    if (ldt != null) {
                        return ldt;
                    }
                    ZonedDateTime zdt = readZonedDateTimeX(len);
                    if (zdt != null) {
                        return zdt.toLocalDateTime();
                    }
                    break;
                default:
                    break;
            }
        }

        String str = readString();
        if (str.isEmpty() || "null".equals(str)) {
            wasNull = true;
            return null;
        }

        DateTimeFormatter formatter = context.getDateFormatter();
        if (formatter != null) {
            if (!context.formatHasHour) {
                return LocalDateTime.of(
                        LocalDate.parse(str, formatter),
                        LocalTime.MIN
                );
            }
            return LocalDateTime.parse(str, formatter);
        }

        if (IOUtils.isNumber(str)) {
            long millis = Long.parseLong(str);

            if (context.formatUnixTime) {
                millis *= 1000L;
            }

            Instant instant = Instant.ofEpochMilli(millis);
            return LocalDateTime.ofInstant(instant, context.getZoneId());
        }

        throw new JSONException(info("read LocalDateTime error " + str));
    }

    public ZonedDateTime readZonedDateTime() {
        if (isInt()) {
            long millis = readInt64Value();
            if (context.formatUnixTime) {
                millis *= 1000L;
            }
            Instant instant = Instant.ofEpochMilli(millis);
            return instant.atZone(context.getZoneId());
        }

        if (ch == '"' || ch == '\'') {
            if (context.dateFormat == null
                    || context.formatyyyyMMddhhmmss19
                    || context.formatyyyyMMddhhmmssT19
                    || context.formatyyyyMMdd8
                    || context.formatISO8601) {
                int len = getStringLength();
                LocalDateTime ldt = null;
                switch (len) {
                    case 8:
                        ldt = readLocalDate8();
                        break;
                    case 9:
                        ldt = readLocalDate9();
                        break;
                    case 10:
                        ldt = readLocalDate10();
                        break;
                    case 11:
                        ldt = readLocalDate11();
                        break;
                    case 16:
                        ldt = readLocalDateTime16();
                        break;
                    case 17:
                        ldt = readLocalDateTime17();
                        break;
                    case 18:
                        ldt = readLocalDateTime18();
                        break;
                    default:
                        ZonedDateTime zdt = readZonedDateTimeX(len);
                        if (zdt != null) {
                            return zdt;
                        }
                        break;
                }
                if (ldt != null) {
                    return ZonedDateTime.of(ldt, context.getZoneId());
                }
            }

            String str = readString();
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }

            DateTimeFormatter formatter = context.getDateFormatter();
            if (formatter != null) {
                if (!context.formatHasHour) {
                    LocalDate localDate = LocalDate.parse(str, formatter);
                    return ZonedDateTime.of(localDate, LocalTime.MIN, context.getZoneId());
                }
                LocalDateTime localDateTime = LocalDateTime.parse(str, formatter);
                return ZonedDateTime.of(localDateTime, context.getZoneId());
            }

            if (IOUtils.isNumber(str)) {
                long millis = Long.parseLong(str);
                if (context.formatUnixTime) {
                    millis *= 1000L;
                }
                Instant instant = Instant.ofEpochMilli(millis);
                return instant.atZone(context.getZoneId());
            }

            return ZonedDateTime.parse(str);
        }
        throw new JSONException("TODO : " + ch);
    }

    public LocalTime readLocalTime() {
        if (nextIfNull()) {
            return null;
        }

        if (isInt()) {
            long millis = readInt64Value();
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = instant.atZone(context.getZoneId());
            return zdt.toLocalTime();
        }

        int len = getStringLength();
        switch (len) {
            case 5:
                return readLocalTime5();
            case 8:
                return readLocalTime8();
            case 10:
                return readLocalTime10();
            case 11:
                return readLocalTime11();
            case 12:
                return readLocalTime12();
            case 18:
                return readLocalTime18();
            case 19:
                return readLocalDateTime19()
                        .toLocalTime();
            default:
                break;
        }

        String str = readString();
        if (str.isEmpty() || "null".equals(str)) {
            return null;
        }

        if (IOUtils.isNumber(str)) {
            long millis = Long.parseLong(str);
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = instant.atZone(context.getZoneId());
            return zdt.toLocalTime();
        }

        throw new JSONException("not support len : " + len);
    }

    protected abstract int getStringLength();

    public Instant readInstant() {
        if (nextIfNull()) {
            return null;
        }

        if (isNumber()) {
            long millis = readInt64Value();
            if (context.formatUnixTime) {
                millis *= 1000L;
            }
            return Instant.ofEpochMilli(millis);
        }

        if (isObject()) {
            return (Instant) getObjectReader(Instant.class)
                    .createInstance(
                            readObject(),
                            0L
                    );
        }

        ZonedDateTime zdt = readZonedDateTime();
        if (zdt == null) {
            return null;
        }

        return Instant.ofEpochSecond(
                zdt.toEpochSecond(),
                zdt.toLocalTime().getNano());
    }

    public long readMillisFromString() {
        String format = context.dateFormat;
        if (format == null
                || context.formatyyyyMMddhhmmss19
                || context.formatyyyyMMddhhmmssT19
                || context.formatyyyyMMdd8
                || context.formatISO8601) {
            int len = getStringLength();
            LocalDateTime ldt = null;
            switch (len) {
                case 8: {
                    ldt = readLocalDate8();
                    if (ldt == null) {
                        throw new JSONException("TODO : " + readString());
                    }
                    break;
                }
                case 9: {
                    ldt = readLocalDate9();
                    break;
                }
                case 10: {
                    ldt = readLocalDate10();
                    if (ldt == null) {
                        String str = readString();
                        if ("0000-00-00".equals(str)) {
                            return 0;
                        }
                        if (IOUtils.isNumber(str)) {
                            return Long.parseLong(str);
                        }
                        throw new JSONException("TODO : " + str);
                    }
                    break;
                }
                case 11: {
                    ldt = readLocalDate11();
                    break;
                }
                case 16: {
                    ldt = readLocalDateTime16();
                    break;
                }
                case 17: {
                    ldt = readLocalDateTime17();
                    break;
                }
                case 18: {
                    ldt = readLocalDateTime18();
                    break;
                }
                case 19: {
                    ldt = readLocalDateTime19();
                }
                default:
                    break;
            }

            ZonedDateTime zdt = null;
            if (ldt != null) {
                zdt = ZonedDateTime.ofLocal(ldt, context.getZoneId(), null);
            } else if (len >= 20) {
                zdt = readZonedDateTimeX(len);
            }

            if (zdt != null) {
                long seconds = zdt.toEpochSecond();
                int nanos = zdt.toLocalTime().getNano();
                if (seconds < 0 && nanos > 0) {
                    long millis = Math.multiplyExact(seconds + 1, 1000);
                    long adjustment = nanos / 1000_000 - 1000;
                    return Math.addExact(millis, adjustment);
                } else {
                    long millis = Math.multiplyExact(seconds, 1000);
                    return Math.addExact(millis, nanos / 1000_000);
                }
            }
        }

        String str = readString();

        if (str.isEmpty() || "null".equals(str)) {
            wasNull = true;
            return 0;
        }

        if (context.formatMillis || context.formatUnixTime) {
            long millis = Long.parseLong(str);
            if (context.formatUnixTime) {
                millis *= 1000L;
            }
            return millis;
        }

        if (format != null && !format.isEmpty()) {
            SimpleDateFormat utilFormat = new SimpleDateFormat(format);
            try {
                return utilFormat
                        .parse(str)
                        .getTime();
            } catch (ParseException e) {
                throw new JSONException("parse date error, " + str + ", expect format " + utilFormat);
            }
        }
        if ("0000-00-00T00:00:00".equals(str)
                || "0001-01-01T00:00:00+08:00".equals(str)) {
            return 0;
        }

        if (str.startsWith("/Date(") && str.endsWith(")/")) {
            String dotnetDateStr = str.substring(6, str.length() - 2);
            int i = dotnetDateStr.indexOf('+');
            if (i == -1) {
                i = dotnetDateStr.indexOf('-');
            }
            if (i != -1) {
                dotnetDateStr = dotnetDateStr.substring(0, i);
            }
            return Long.parseLong(dotnetDateStr);
        } else if (IOUtils.isNumber(str)) {
            return Long.parseLong(str);
        }

        throw new JSONException(info("format " + format + " not support, input " + str));
    }

    protected abstract LocalDateTime readLocalDateTime16();

    protected abstract LocalDateTime readLocalDateTime17();

    protected abstract LocalDateTime readLocalDateTime18();

    protected abstract LocalDateTime readLocalDateTime19();

    protected abstract LocalDateTime readLocalDateTimeX(int len);

    protected abstract LocalTime readLocalTime5();

    protected abstract LocalTime readLocalTime8();

    protected abstract LocalTime readLocalTime10();

    protected abstract LocalTime readLocalTime11();

    protected abstract LocalTime readLocalTime12();

    protected abstract LocalTime readLocalTime18();

    protected abstract LocalDateTime readLocalDate8();

    protected abstract LocalDateTime readLocalDate9();

    protected abstract LocalDateTime readLocalDate10();

    protected abstract LocalDateTime readLocalDate11();

    protected abstract ZonedDateTime readZonedDateTimeX(int len);

    public void readNumber(ValueConsumer consumer, boolean quoted) {
        readNumber0();
        Number number = getNumber();
        consumer.accept(number);
    }

    public void readString(ValueConsumer consumer, boolean quoted) {
        String str = readString(); //
        if (quoted) {
            consumer.accept(JSON.toJSONString(str));
        } else {
            consumer.accept(str);
        }
    }

    protected abstract void readNumber0();

    public abstract String readString();

    public char readCharValue() {
        String str = readString();
        if (str == null || str.isEmpty()) {
            wasNull = true;
            return '\0';
        }
        return str.charAt(0);
    }

    public abstract void readNull();

    public abstract boolean readIfNull();

    public abstract String getString();

    public boolean wasNull() {
        return wasNull;
    }

    public <T> T read(Type type) {
        boolean fieldBased = (context.features & Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = context.provider.getObjectReader(type, fieldBased);
        return (T) objectReader.readObject(this, null, null, 0);
    }

    public void read(List list) {
        if (!nextIfMatch('[')) {
            throw new JSONException("illegal input, offset " + offset + ", char " + ch);
        }

        for (; ; ) {
            if (nextIfMatch(']')) {
                break;
            }
            Object item = readAny();
            list.add(item);

            if (nextIfMatch(',')) {
                continue;
            }
        }

        nextIfMatch(',');
    }

    public void read(Collection list) {
        if (!nextIfMatch('[')) {
            throw new JSONException("illegal input, offset " + offset + ", char " + ch);
        }

        for (; ; ) {
            if (nextIfMatch(']')) {
                break;
            }
            Object item = readAny();
            list.add(item);

            if (nextIfMatch(',')) {
                continue;
            }
        }

        nextIfMatch(',');
    }

    public void readObject(Object object, Feature... features) {
        long featuresLong = 0;
        for (Feature feature : features) {
            featuresLong |= feature.mask;
        }
        readObject(object, featuresLong);
    }

    public void readObject(Object object, long features) {
        if (object == null) {
            throw new JSONException("object is null");
        }
        Class objectClass = object.getClass();
        boolean fieldBased = ((context.features | features) & Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = context.provider.getObjectReader(objectClass, fieldBased);
        if (objectReader instanceof ObjectReaderBean) {
            ObjectReaderBean objectReaderBean = (ObjectReaderBean) objectReader;
            objectReaderBean.readObject(this, object, features);
        } else if (object instanceof Map) {
            read((Map) object, features);
        } else {
            throw new JSONException("read object not support");
        }
    }

    public void read(Map object, long features) {
        boolean match = nextIfMatch('{');
        boolean typeRedirect = false;
        if (!match) {
            if (typeRedirect = isTypeRedirect()) {
                setTypeRedirect(false);
            } else {
                if (isString()) {
                    String str = readString();
                    if (str.isEmpty()) {
                        return;
                    }
                }
                throw new JSONException("illegal input， offset " + offset + ", char " + ch);
            }
        }

        for_:
        for (int i = 0; ; ++i) {
            if (ch == '/') {
                skipLineComment();
            }

            if (nextIfMatch('}')) {
                break;
            }

            if (i != 0 && !comma) {
                throw new JSONException(info());
            }

            Object name;
            if (match || typeRedirect) {
                name = readFieldName();
            } else {
                name = getFieldName();
                match = true;
            }

            if (name == null) {
                if (isNumber()) {
                    name = readNumber();
                    if ((context.features & Feature.NonStringKeyAsString.mask) != 0) {
                        name = name.toString();
                    }
                } else {
                    if ((context.features & Feature.AllowUnQuotedFieldNames.mask) != 0) {
                        name = readFieldNameUnquote();
                    } else {
                        throw new JSONException(info("not allow unquoted fieldName"));
                    }
                }
                if (ch == ':') {
                    next();
                }
            }

            comma = false;
            Object value;
            switch (ch) {
                case '-':
                case '+':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '.':
                    value = readNumber();
                    break;
                case '[':
                    value = readArray();
                    break;
                case '{':
                    value = readObject();
                    break;
                case '"':
                case '\'':
                    value = readString();
                    break;
                case 't':
                case 'f':
                    value = readBoolValue();
                    break;
                case 'n':
                    value = readNullOrNewDate();
                    break;
                case '/':
                    next();
                    if (ch == '/') {
                        skipLineComment();
                    } else {
                        throw new JSONException("FASTJSON" + JSON.VERSION + "input not support " + ch + ", offset " + offset);
                    }
                    continue for_;
                case 'S':
                    if (nextIfSet()) {
                        value = read(HashSet.class);
                    } else {
                        throw new JSONException("FASTJSON" + JSON.VERSION + "error, offset " + offset + ", char " + ch);
                    }
                    break;
                default:
                    throw new JSONException("FASTJSON" + JSON.VERSION + "error, offset " + offset + ", char " + ch);
            }
            Object origin = object.put(name, value);
            if (origin != null) {
                long contextFeatures = features | context.getFeatures();
                if ((contextFeatures & JSONReader.Feature.DuplicateKeyValueAsArray.mask) != 0) {
                    if (origin instanceof Collection) {
                        ((Collection) origin).add(value);
                        object.put(name, value);
                    } else {
                        JSONArray array = JSONArray.of(origin, value);
                        object.put(name, array);
                    }
                }
            }
        }
    }

    public <T> T read(Class<T> type) {
        boolean fieldBased = (context.features & Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = context.provider.getObjectReader(type, fieldBased);
        return (T) objectReader.readObject(this, null, null, 0);
    }

    public Map<String, Object> readObject() {
        nextIfObjectStart();
        Map object;
        if (context.objectSupplier == null) {
            if ((context.features & Feature.UseNativeObject.mask) != 0) {
                object = new HashMap();
            } else {
                object = new JSONObject();
            }
        } else {
            object = (Map) context.objectSupplier.get();
        }

        for_:
        for (int i = 0; ; ++i) {
            if (ch == '}') {
                next();
                break;
            }

            String name = readFieldName();
            if (name == null) {
                name = readFieldNameUnquote();
                nextIfMatch(':');
            }

            if (i == 0 && (context.features & Feature.ErrorOnNotSupportAutoType.mask) != 0 && "@type".equals(name)) {
                String typeName = readString();
                throw new JSONException("autoType not support : " + typeName);
            }
            Object val;
            switch (ch) {
                case '-':
                case '+':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    readNumber0();
                    val = getNumber();
                    break;
                case '[':
                    val = readArray();
                    break;
                case '{':
                    val = readObject();
                    break;
                case '"':
                case '\'':
                    val = readString();
                    break;
                case 't':
                case 'f':
                    val = readBoolValue();
                    break;
                case 'n':
                    readNull();
                    val = null;
                    break;
                case '/':
                    next();
                    if (ch == '/') {
                        skipLineComment();
                    }
                    continue for_;
                default:
                    throw new JSONException(info("illegal input " + ch));
            }
            object.put(name, val);
        }

        if (ch == ',') {
            this.comma = true;
            next();
        }

        return object;
    }

    public abstract void skipLineComment();

    public Boolean readBool() {
        if (isNull()) {
            readNull();
            return null;
        }

        boolean boolValue = readBoolValue();
        if (!boolValue && wasNull) {
            return null;
        }
        return boolValue;
    }

    public boolean readBoolValue() {
        wasNull = false;
        boolean val;
        if (ch == 't') {
            next();
            char c1 = ch;
            next();
            char c2 = ch;
            next();
            char c3 = ch;
            if (c1 == 'r' && c2 == 'u' || c3 == 'e') {
                val = true;
            } else {
                throw new JSONException("syntax error : " + ch);
            }
        } else if (ch == 'f') {
            next();
            char c1 = ch;
            next();
            char c2 = ch;
            next();
            char c3 = ch;
            next();
            char c4 = ch;
            if (c1 == 'a' && c2 == 'l' || c3 == 's' || c4 == 'e') {
                val = false;
            } else {
                throw new JSONException("syntax error : " + ch);
            }
        } else if (ch == '-' || (ch >= '0' && ch <= '9')) {
            readNumber();
            return valueType == JSON_TYPE_INT
                    && mag1 == 0
                    && mag2 == 0
                    && mag3 == 1;
        } else if (ch == 'n') {
            wasNull = true;
            readNull();
            return false;
        } else if (ch == '"') {
            int len = getStringLength();
            if (len == 1) {
                next();
                if (ch == '0' || ch == 'N') {
                    next();
                    next();
                    nextIfMatch(',');
                    return false;
                } else if (ch == '1' || ch == 'Y') {
                    next();
                    next();
                    nextIfMatch(',');
                    return true;
                }
                throw new JSONException("can not convert to boolean : " + ch);
            }
            String str = readString();
            if ("true".equalsIgnoreCase(str)) {
                return true;
            }

            if ("false".equalsIgnoreCase(str)) {
                return false;
            }

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                wasNull = true;
                return false;
            }

            throw new JSONException("can not convert to boolean : " + str);
        } else {
            throw new JSONException("syntax error : " + ch);
        }

        next();

        nextIfMatch(',');

        return val;
    }

    public Object readAny() {
        return read(Object.class);
    }

    public List readArray(Type itemType) {
        if (nextIfNull()) {
            return null;
        }

        List list = new ArrayList();
        if (!nextIfMatch('[')) {
            throw new JSONException("syntax error : " + ch);
        }

        for (; ; ) {
            if (nextIfMatch(']')) {
                break;
            }
            Object item = read(itemType);
            list.add(item);

            if (ch == '}') {
                throw new JSONException("illegal input : " + ch + ", offset " + getOffset());
            }
        }

        if (ch == ',') {
            this.comma = true;
            next();
        }

        return list;
    }

    public void readArray(List list, Type itemType) {
        if (nextIfMatch('[')) {
            for (; ; ) {
                if (nextIfMatch(']')) {
                    break;
                }
                Object item = read(itemType);
                list.add(item);

                if (ch == '}') {
                    throw new JSONException(info());
                }
            }

            if (ch == ',') {
                this.comma = true;
                next();
            }
            return;
        }

        if (isString()) {
            String str = readString();
            if (itemType == String.class) {
                list.add(str);
            } else {
                Function typeConvert = context.getProvider().getTypeConvert(String.class, itemType);
                if (typeConvert == null) {
                    throw new JSONException(info("not support input " + str));
                }
                if (str.indexOf(',') != -1) {
                    String[] items = str.split(",");
                    for (String strItem : items) {
                        Object item = typeConvert.apply(strItem);
                        list.add(item);
                    }
                } else {
                    Object item = typeConvert.apply(str);
                    list.add(item);
                }
            }
        } else {
            Object item = read(itemType);
            list.add(item);
        }

        if (ch == ',') {
            this.comma = true;
            next();
        }
    }

    public List readArray() {
        next();

        int i = 0;
        List<Object> list = null;
        Object first = null, second = null;

        _for:
        for (; ; ++i) {
            Object val;
            switch (ch) {
                case ']':
                    next();
                    break _for;
                case '[':
                    val = readArray();
                    break;
                case '{':
                    if (context.autoTypeBeforeHandler != null || (context.features & Feature.SupportAutoType.mask) != 0) {
                        val = ObjectReaderImplObject.INSTANCE.readObject(this, null, null, 0);
                    } else {
                        val = readObject();
                    }
                    break;
                case '\'':
                case '"':
                    val = readString();
                    break;
                case '-':
                case '+':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    readNumber0();
                    val = getNumber();
                    break;
                case 't':
                case 'f':
                    val = readBoolValue();
                    break;
                case 'n': {
                    readNull();
                    val = null;
                    break;
                }
                default:
                    throw new JSONException("TODO : " + ch);
            }

            if (i == 0) {
                first = val;
            } else if (i == 1) {
                second = val;
            } else if (i == 2) {
                if (context.arraySupplier != null) {
                    list = context.arraySupplier.get();
                } else {
                    list = new JSONArray();
                }

                list.add(first);
                list.add(second);
                list.add(val);
            } else {
                list.add(val);
            }
        }

        if (list == null) {
            if (context.arraySupplier != null) {
                list = context.arraySupplier.get();
            } else {
                if (context.isEnabled(Feature.UseNativeObject)) {
                    list = i == 2 ? new ArrayList(2) : new ArrayList(1);
                } else {
                    list = i == 2 ? new JSONArray(2) : new JSONArray(1);
                }
            }

            if (i == 1) {
                list.add(first);
            } else if (i == 2) {
                list.add(first);
                list.add(second);
            }
        }

        if (ch == ',') {
            this.comma = true;
            next();
        }

        return list;
    }

    public BigInteger getBigInteger() {
        Number number = getNumber();

        if (number == null) {
            return null;
        }

        if (number instanceof BigInteger) {
            return (BigInteger) number;
        }
        return BigInteger.valueOf(number.longValue());
    }

    public BigDecimal getBigDecimal() {
        if (wasNull) {
            return null;
        }

        switch (valueType) {
            case JSON_TYPE_INT: {
                if (mag1 == 0 && mag2 == 0 && mag3 >= 0) {
                    return BigDecimal.valueOf(negative ? -mag3 : mag3);
                }
                int[] mag;
                if (mag0 == 0) {
                    if (mag1 == 0) {
                        long v3 = mag3 & LONG_MASK;
                        long v2 = mag2 & LONG_MASK;

                        if (v2 >= Integer.MIN_VALUE && v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            return BigDecimal.valueOf(negative ? -v23 : v23);
                        }
                        mag = new int[]{mag2, mag3};
                    } else {
                        mag = new int[]{mag1, mag2, mag3};
                    }
                } else {
                    mag = new int[]{mag0, mag1, mag2, mag3};
                }

                return new BigDecimal(getBigInt(negative, mag));
            }
            case JSON_TYPE_DEC: {
                BigDecimal decimal = null;

                if (exponent == 0 && mag0 == 0 && mag1 == 0) {
                    if (mag2 == 0 && mag3 >= 0) {
                        int unscaledVal = negative ? -mag3 : mag3;
                        decimal = BigDecimal.valueOf(unscaledVal, scale);
                    } else {
                        long v3 = mag3 & LONG_MASK;
                        long v2 = mag2 & LONG_MASK;

                        if (v2 >= Integer.MIN_VALUE && v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            long unscaledVal = negative ? -v23 : v23;
                            decimal = BigDecimal.valueOf(unscaledVal, scale);
                        }
                    }
                }

                if (decimal == null) {
                    int[] mag = mag0 == 0
                            ? mag1 == 0
                            ? mag2 == 0
                            ? new int[]{mag3}
                            : new int[]{mag2, mag3}
                            : new int[]{mag1, mag2, mag3}
                            : new int[]{mag0, mag1, mag2, mag3};
                    BigInteger bigInt = getBigInt(negative, mag);
                    decimal = new BigDecimal(bigInt, scale);
                }

                if (exponent != 0) {
                    double doubleValue = Double.parseDouble(
                            decimal + "E" + exponent);
                    return BigDecimal.valueOf(doubleValue);
                }

                return decimal;
            }
            case JSON_TYPE_BIG_DEC: {
                return new BigDecimal(stringValue);
            }
            case JSON_TYPE_BOOL:
                return boolValue ? BigDecimal.ONE : BigDecimal.ZERO;
            case JSON_TYPE_STRING: {
                try {
                    return new BigDecimal(stringValue);
                } catch (NumberFormatException ex) {
                    throw new JSONException(info("read decimal error, value " + stringValue), ex);
                }
            }
            case JSON_TYPE_OBJECT: {
                JSONObject object = (JSONObject) complex;
                BigDecimal decimal = object.getBigDecimal("value");
                if (decimal == null) {
                    decimal = object.getBigDecimal("$numberDecimal");
                }
                if (decimal != null) {
                    return decimal;
                }
                throw new JSONException("TODO : " + valueType);
            }
            default:
                throw new JSONException("TODO : " + valueType);
        }
    }

    public Number getNumber() {
        if (wasNull) {
            return null;
        }

        switch (valueType) {
            case JSON_TYPE_INT:
            case JSON_TYPE_INT64: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                    int intVlaue;
                    if (negative) {
                        if (mag3 < 0) {
                            return -(mag3 & 0xFFFFFFFFL);
                        }
                        intVlaue = -mag3;
                    } else {
                        if (mag3 < 0) {
                            return mag3 & 0xFFFFFFFFL;
                        }
                        intVlaue = mag3;
                    }
                    if (valueType == JSON_TYPE_INT64) {
                        return Long.valueOf(intVlaue);
                    }
                    return Integer.valueOf(intVlaue);
                }
                int[] mag;
                if (mag0 == 0) {
                    if (mag1 == 0) {
                        long v3 = mag3 & LONG_MASK;
                        long v2 = mag2 & LONG_MASK;

                        if (v2 >= Integer.MIN_VALUE && v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            return negative ? -v23 : v23;
                        }
                        mag = new int[]{mag2, mag3};
                    } else {
                        mag = new int[]{mag1, mag2, mag3};
                    }
                } else {
                    mag = new int[]{mag0, mag1, mag2, mag3};
                }

                return getBigInt(negative, mag);
            }
            case JSON_TYPE_INT16: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 >= 0) {
                    int intValue = negative ? -mag3 : mag3;
                    return Short.valueOf((short) intValue);
                }
                throw new JSONException(info("shortValue overflow"));
            }
            case JSON_TYPE_INT8: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 >= 0) {
                    int intValue = negative ? -mag3 : mag3;
                    return Byte.valueOf((byte) intValue);
                }
                throw new JSONException(info("shortValue overflow"));
            }
            case JSON_TYPE_DEC: {
                BigDecimal decimal = null;

                if (mag0 == 0 && mag1 == 0) {
                    if (mag2 == 0 && mag3 >= 0) {
                        int unscaledVal = negative ? -mag3 : mag3;

                        if (exponent == 0) {
                            if ((context.features & Feature.UseBigDecimalForFloats.mask) != 0) {
                                if (scale == 1) {
                                    int small = unscaledVal % 10;
                                    return unscaledVal / 10 + FLOAT_SMALL_1[small];
                                } else if (scale == 2) {
                                    int small = unscaledVal % 100;
                                    return unscaledVal / 100 + FLOAT_SMALL_2[small];
                                } else if (scale == 3) {
                                    int small = unscaledVal % 1000;
                                    return unscaledVal / 1000 + FLOAT_SMALL_3[small];
                                } else if (scale == 4) {
                                    return floatValue4(unscaledVal);
                                } else if (scale == 5) {
                                    return floatValue5(unscaledVal);
                                }
                            } else if ((context.features & Feature.UseBigDecimalForDoubles.mask) != 0) {
                                if (scale == 1) {
                                    int small = unscaledVal % 10;
                                    return unscaledVal / 10 + DOUBLE_SMALL_1[small];
                                } else if (scale == 2) {
                                    int small = unscaledVal % 100;
                                    return unscaledVal / 100 + DOUBLE_SMALL_2[small];
                                } else if (scale == 3) {
                                    int small = unscaledVal % 1000;
                                    return unscaledVal / 1000 + DOUBLE_SMALL_3[small];
                                }
                            }
                        }
                        decimal = BigDecimal.valueOf(unscaledVal, scale);
                    } else {
                        long v3 = mag3 & LONG_MASK;
                        long v2 = mag2 & LONG_MASK;

                        if (v2 >= Integer.MIN_VALUE && v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            long unscaledVal = negative ? -v23 : v23;

                            if (exponent == 0) {
                                if ((context.features & Feature.UseBigDecimalForFloats.mask) != 0) {
                                    if (scale == 1) {
                                        int small = (int) (unscaledVal % 10);
                                        return unscaledVal / 10 + FLOAT_SMALL_1[small];
                                    } else if (scale == 2) {
                                        int small = (int) (unscaledVal % 100);
                                        return unscaledVal / 100 + FLOAT_SMALL_2[small];
                                    } else if (scale == 3) {
                                        int small = (int) (unscaledVal % 1000);
                                        return unscaledVal / 1000 + FLOAT_SMALL_3[small];
                                    } else if (scale == 4) {
                                        return floatValue4(unscaledVal);
                                    } else if (scale == 5) {
                                        return floatValue5(unscaledVal);
                                    }
                                } else if ((context.features & Feature.UseBigDecimalForDoubles.mask) != 0) {
                                    if (scale == 1) {
                                        int small = (int) (unscaledVal % 10);
                                        return unscaledVal / 10 + DOUBLE_SMALL_1[small];
                                    } else if (scale == 2) {
                                        int small = (int) (unscaledVal % 100);
                                        return unscaledVal / 100 + DOUBLE_SMALL_2[small];
                                    } else if (scale == 3) {
                                        int small = (int) (unscaledVal % 1000);
                                        return unscaledVal / 1000 + DOUBLE_SMALL_3[small];
                                    }
                                }
                            }
                            decimal = BigDecimal.valueOf(unscaledVal, scale);
                        }
                    }
                }

                if (decimal == null) {
                    int[] mag = mag0 == 0
                            ? mag1 == 0
                            ? mag2 == 0
                            ? new int[]{mag3}
                            : new int[]{mag2, mag3}
                            : new int[]{mag1, mag2, mag3}
                            : new int[]{mag0, mag1, mag2, mag3};
                    BigInteger bigInt = getBigInt(negative, mag);
                    int adjustedScale = scale - exponent;
                    decimal = new BigDecimal(bigInt, adjustedScale);
                }

                if (exponent != 0) {
                    double doubleValue = Double.parseDouble(
                            decimal + "E" + exponent);
                    return Double.valueOf(doubleValue);
                }

                if ((context.features & Feature.UseBigDecimalForFloats.mask) != 0) {
                    return decimal.floatValue();
                }

                if ((context.features & Feature.UseBigDecimalForDoubles.mask) != 0) {
                    return decimal.doubleValue();
                }

                return decimal;
            }
            case JSON_TYPE_BIG_DEC: {
                if (scale > 0) {
                    return new BigDecimal(stringValue);
                } else {
                    return new BigInteger(stringValue);
                }
            }
            case JSON_TYPE_FLOAT:
            case JSON_TYPE_DOUBLE: {
                int[] mag = mag0 == 0
                        ? mag1 == 0
                        ? mag2 == 0
                        ? new int[]{mag3}
                        : new int[]{mag2, mag3}
                        : new int[]{mag1, mag2, mag3}
                        : new int[]{mag0, mag1, mag2, mag3};
                BigInteger bigInt = getBigInt(negative, mag);
                BigDecimal decimal = new BigDecimal(bigInt, scale);

                if (valueType == JSON_TYPE_FLOAT) {
                    if (exponent != 0) {
                        float floatValueValue = Float.parseFloat(
                                decimal + "E" + exponent);
                        return Float.valueOf(floatValueValue);
                    }

                    return decimal.floatValue();
                }

                if (exponent != 0) {
                    double doubleValue = Double.parseDouble(
                            decimal + "E" + exponent);
                    return Double.valueOf(doubleValue);
                }
                return decimal.doubleValue();
            }
            case JSON_TYPE_BOOL:
                return boolValue ? 1 : 0;
            case JSON_TYPE_NULL:
                return null;
            case JSON_TYPE_STRING: {
                return toInt64(stringValue);
            }
            case JSON_TYPE_OBJECT: {
                return toNumber((Map) complex);
            }
            case JSON_TYPE_ARRAY: {
                return toNumber((List) complex);
            }
            default:
                throw new JSONException("TODO : " + valueType);
        }
    }

    @Override
    public void close() {
    }

    static BigInteger getBigInt(boolean negative, int[] mag) {
        int signum = mag.length == 0 ? 0 : negative ? -1 : 1;

        final int bitLength;
        if (mag.length == 0) {
            bitLength = 0; // offset by one to initialize
        } else {
            // Calculate the bit length of the magnitude
            int bitLengthForInt = 32 - Integer.numberOfLeadingZeros(mag[0]);
            int magBitLength = ((mag.length - 1) << 5) + bitLengthForInt;
            if (signum < 0) {
                // Check if magnitude is a power of two
                boolean pow2 = (Integer.bitCount(mag[0]) == 1);
                for (int i = 1; i < mag.length && pow2; i++) {
                    pow2 = (mag[i] == 0);
                }
                bitLength = (pow2 ? magBitLength - 1 : magBitLength);
            } else {
                bitLength = magBitLength;
            }
        }
        int byteLen = bitLength / 8 + 1;

        byte[] bytes = new byte[byteLen];
        for (int i = byteLen - 1, bytesCopied = 4, nextInt = 0, intIndex = 0; i >= 0; i--) {
            if (bytesCopied == 4) {
                // nextInt = getInt(intIndex++
                int n = intIndex++;
                if (n < 0) {
                    nextInt = 0;
                } else if (n >= mag.length) {
                    nextInt = signum < 0 ? -1 : 0;
                } else {
                    int magInt = mag[mag.length - n - 1];
                    if (signum >= 0) {
                        nextInt = magInt;
                    } else {
                        int firstNonzeroIntNum;
                        {
                            int j;
                            int mlen = mag.length;
                            for (j = mlen - 1; j >= 0 && mag[j] == 0; j--) {
                                // empty
                            }
                            firstNonzeroIntNum = mlen - j - 1;
                        }

                        if (n <= firstNonzeroIntNum) {
                            nextInt = -magInt;
                        } else {
                            nextInt = ~magInt;
                        }
                    }
                }

                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                bytesCopied++;
            }
            bytes[i] = (byte) nextInt;
        }

        return new BigInteger(bytes);
    }

    protected final int toInt32(String val) {
        if (IOUtils.isNumber(val)) {
            return Integer.parseInt(val);
        }
        throw new JSONException("parseInt error, value : " + val);
    }

    protected final long toInt64(String val) {
        if (IOUtils.isNumber(val)) {
            return Long.parseLong(val);
        }
        throw new JSONException("parseLong error, value : " + val);
    }

    protected final long toLong(Map map) {
        Object val = map.get("val");
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        throw new JSONException("parseLong error, value : " + map);
    }

    protected final int toInt(List list) {
        if (list.size() == 1) {
            Object val = list.get(0);
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
            if (val instanceof String) {
                return Integer.parseInt((String) val);
            }
        }

        throw new JSONException("parseLong error, field : value " + list);
    }

    protected final Number toNumber(Map map) {
        Object val = map.get("val");
        if (val instanceof Number) {
            return (Number) val;
        }
        return null;
    }

    protected final Number toNumber(List list) {
        if (list.size() == 1) {
            Object val = list.get(0);
            if (val instanceof Number) {
                return (Number) val;
            }

            if (val instanceof String) {
                return new BigDecimal((String) val);
            }
        }
        return null;
    }

    protected final String toString(List array) {
        JSONWriter writer = JSONWriter.of();
        writer.write(array);
        return writer.toString();
    }

    protected final String toString(Map object) {
        JSONWriter writer = JSONWriter.of();
        writer.write(object);
        return writer.toString();
    }

    public static JSONReader of(byte[] utf8Bytes) {
        Context context = createReadContext();
        return new JSONReaderUTF8(context, utf8Bytes, 0, utf8Bytes.length);
    }

    public static JSONReader of(JSONReader.Context context, byte[] utf8Bytes) {
        return new JSONReaderUTF8(context, utf8Bytes, 0, utf8Bytes.length);
    }

    public static JSONReader of(char[] chars) {
        return new JSONReaderUTF16(
                JSONFactory.createReadContext(),
                null,
                chars,
                0,
                chars.length);
    }

    public static JSONReader of(Context context, char[] chars) {
        return new JSONReaderUTF16(
                context,
                null,
                chars,
                0,
                chars.length
        );
    }

    public static JSONReader ofJSONB(byte[] jsonbBytes) {
        return new JSONReaderJSONB(
                JSONFactory.createReadContext(),
                jsonbBytes,
                0,
                jsonbBytes.length);
    }

    public static JSONReader ofJSONB(JSONReader.Context context, byte[] jsonbBytes) {
        return new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);
    }

    public static JSONReader ofJSONB(byte[] jsonbBytes, JSONReader.Feature... features) {
        Context context = JSONFactory.createReadContext();
        context.config(features);
        return new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);
    }

    public static JSONReader ofJSONB(byte[] bytes, int offset, int length) {
        return new JSONReaderJSONB(
                JSONFactory.createReadContext(),
                bytes,
                offset,
                length);
    }

    public static JSONReader ofJSONB(byte[] bytes, int offset, int length, SymbolTable symbolTable) {
        return new JSONReaderJSONB(
                JSONFactory.createReadContext(symbolTable),
                bytes,
                offset,
                length);
    }

    public static JSONReader of(byte[] bytes, int offset, int length, Charset charset) {
        Context ctx = JSONFactory.createReadContext();

        if (charset == StandardCharsets.UTF_8) {
            return new JSONReaderUTF8(ctx, bytes, offset, length);
        }

        if (charset == StandardCharsets.UTF_16) {
            return new JSONReaderUTF16(ctx, bytes, offset, length);
        }

        if (charset == StandardCharsets.US_ASCII) {
            return new JSONReaderASCII(ctx, null, bytes, offset, length);
        }

        throw new JSONException("not support charset " + charset);
    }

    public static JSONReader of(byte[] bytes, int offset, int length) {
        return new JSONReaderUTF8(JSONFactory.createReadContext(), bytes, offset, length);
    }

    public static JSONReader of(char[] chars, int offset, int length) {
        return new JSONReaderUTF16(JSONFactory.createReadContext(), null, chars, offset, length);
    }

    public static JSONReader of(InputStream is, Charset charset) {
        Context context = JSONFactory.createReadContext();

        if (charset == StandardCharsets.UTF_8 || charset == null) {
            return new JSONReaderUTF8(context, is);
        }

        if (charset == StandardCharsets.UTF_16) {
            return new JSONReaderUTF16(context, is);
        }

        throw new JSONException("not support charset " + charset);
    }

    public static JSONReader of(java.io.Reader is) {
        return new JSONReaderUTF16(
                JSONFactory.createReadContext(),
                is
        );
    }

    public static JSONReader of(Context context, String str) {
        if (str == null) {
            throw new NullPointerException();
        }

        if (JDKUtils.JVM_VERSION > 8 && JDKUtils.UNSAFE_SUPPORT && str.length() > 1024 * 1024) {
            try {
                byte coder = UnsafeUtils.getStringCoder(str);
                if (coder == 0) {
                    byte[] bytes = UnsafeUtils.getStringValue(str);
                    return new JSONReaderASCII(context, str, bytes, 0, bytes.length);
                }
            } catch (Exception e) {
                throw new JSONException("unsafe get String.coder error");
            }

            return new JSONReaderStr(context, str, 0, str.length());
        }

        final int length = str.length();
        char[] chars;
        if (JDKUtils.JVM_VERSION == 8) {
            chars = JDKUtils.getCharArray(str);
        } else {
            chars = str.toCharArray();
        }

        return new JSONReaderUTF16(context, str, chars, 0, length);
    }

    public static JSONReader of(String str) {
        if (str == null) {
            throw new NullPointerException();
        }

        Context context = JSONFactory.createReadContext();
        if (JDKUtils.JVM_VERSION > 8 && JDKUtils.UNSAFE_SUPPORT) {
            try {
                byte coder = UnsafeUtils.getStringCoder(str);
                if (coder == 0) {
                    byte[] bytes = UnsafeUtils.getStringValue(str);
                    return new JSONReaderASCII(context, str, bytes, 0, bytes.length);
                }
            } catch (Exception e) {
                throw new JSONException("unsafe get String.coder error");
            }

            if (str.length() > 1024 * 1024) {
                return new JSONReaderStr(context, str, 0, str.length());
            }
        }

        final int length = str.length();
        char[] chars;
        if (JDKUtils.JVM_VERSION == 8) {
            chars = JDKUtils.getCharArray(str);
        } else {
            chars = str.toCharArray();
        }

        return new JSONReaderUTF16(context, str, chars, 0, length);
    }

    public static JSONReader of(String str, int offset, int length) {
        if (str == null) {
            throw new NullPointerException();
        }

        Context context = JSONFactory.createReadContext();
        if (JDKUtils.JVM_VERSION > 8 && JDKUtils.UNSAFE_SUPPORT) {
            try {
                byte coder = UnsafeUtils.getStringCoder(str);
                if (coder == 0) {
                    byte[] bytes = UnsafeUtils.getStringValue(str);
                    return new JSONReaderASCII(context, str, bytes, offset, length);
                }
            } catch (Exception e) {
                throw new JSONException("unsafe get String.coder error");
            }

            return new JSONReaderStr(context, str, 0, str.length());
        }

        char[] chars;
        if (JDKUtils.JVM_VERSION == 8) {
            chars = JDKUtils.getCharArray(str);
        } else {
            chars = str.toCharArray();
        }

        return new JSONReaderUTF16(context, str, chars, offset, length);
    }

    void bigInt(char[] chars, int off, int len) {
        int cursor = off, numDigits;

        numDigits = len - cursor;
        if (scale > 0) {
            numDigits--;
        }
        if (numDigits > 38) {
            throw new JSONException("number too large : " + new String(chars, off, numDigits));
        }

        // Process first (potentially short) digit group
        int firstGroupLen = numDigits % 9;
        if (firstGroupLen == 0) {
            firstGroupLen = 9;
        }

        {
            int start = cursor;
            int end = cursor += firstGroupLen;

            char c = chars[start++];
            if (c == '.') {
                c = chars[start++];
                cursor++;
//                    end++;
            }

            int result = c - '0';

            for (int index = start; index < end; index++) {
                c = chars[index];
                if (c == '.') {
                    c = chars[++index];
                    cursor++;
                    if (end < len) {
                        end++;
                    }
                }

                int nextVal = c - '0';
                result = 10 * result + nextVal;
            }
            mag3 = result;
        }

        // Process remaining digit groups
        while (cursor < len) {
            int groupVal;
            {
                int start = cursor;
                int end = cursor += 9;

                char c = chars[start++];
                if (c == '.') {
                    c = chars[start++];
                    cursor++;
                    end++;
                }

                int result = c - '0';

                for (int index = start; index < end; index++) {
                    c = chars[index];
                    if (c == '.') {
                        c = chars[++index];
                        cursor++;
                        end++;
                    }

                    int nextVal = c - '0';
                    result = 10 * result + nextVal;
                }
                groupVal = result;
            }

            // destructiveMulAdd
            long ylong = 1000000000 & LONG_MASK;

            long product = 0;
            long carry = 0;
            for (int i = 3; i >= 0; i--) {
                switch (i) {
                    case 0:
                        product = ylong * (mag0 & LONG_MASK) + carry;
                        mag0 = (int) product;
                        break;
                    case 1:
                        product = ylong * (mag1 & LONG_MASK) + carry;
                        mag1 = (int) product;
                        break;
                    case 2:
                        product = ylong * (mag2 & LONG_MASK) + carry;
                        mag2 = (int) product;
                        break;
                    case 3:
                        product = ylong * (mag3 & LONG_MASK) + carry;
                        mag3 = (int) product;
                        break;
                    default:
                        throw new ArithmeticException("BigInteger would overflow supported range");
                }
                carry = product >>> 32;
            }

            long zlong = groupVal & LONG_MASK;
            long sum = (mag3 & LONG_MASK) + zlong;
            mag3 = (int) sum;

            // Perform the addition
            carry = sum >>> 32;
            for (int i = 2; i >= 0; i--) {
                switch (i) {
                    case 0:
                        sum = (mag0 & LONG_MASK) + carry;
                        mag0 = (int) sum;
                        break;
                    case 1:
                        sum = (mag1 & LONG_MASK) + carry;
                        mag1 = (int) sum;
                        break;
                    case 2:
                        sum = (mag2 & LONG_MASK) + carry;
                        mag2 = (int) sum;
                        break;
                    case 3:
                        sum = (mag3 & LONG_MASK) + carry;
                        mag3 = (int) sum;
                        break;
                    default:
                        throw new ArithmeticException("BigInteger would overflow supported range");
                }
                carry = sum >>> 32;
            }
        }
    }

    void bigInt(byte[] chars, int off, int len) {
        int cursor = off, numDigits;

        numDigits = len - cursor;
        if (scale > 0) {
            numDigits--;
        }
        if (numDigits > 38) {
            throw new JSONException("number too large : " + new String(chars, off, numDigits));
        }

        // Process first (potentially short) digit group
        int firstGroupLen = numDigits % 9;
        if (firstGroupLen == 0) {
            firstGroupLen = 9;
        }

        {
            int start = cursor;
            int end = cursor += firstGroupLen;

            char c = (char) chars[start++];
            if (c == '.') {
                c = (char) chars[start++];
                cursor++;
//                    end++;
            }

            int result = c - '0';

            for (int index = start; index < end; index++) {
                c = (char) chars[index];
                if (c == '.') {
                    c = (char) chars[++index];
                    cursor++;
                    if (end < len) {
                        end++;
                    }
                }

                int nextVal = c - '0';
                result = 10 * result + nextVal;
            }
            mag3 = result;
        }

        // Process remaining digit groups
        while (cursor < len) {
            int groupVal;
            {
                int start = cursor;
                int end = cursor += 9;

                char c = (char) chars[start++];
                if (c == '.') {
                    c = (char) chars[start++];
                    cursor++;
                    end++;
                }

                int result = c - '0';

                for (int index = start; index < end; index++) {
                    c = (char) chars[index];
                    if (c == '.') {
                        c = (char) chars[++index];
                        cursor++;
                        end++;
                    }

                    int nextVal = c - '0';
                    result = 10 * result + nextVal;
                }
                groupVal = result;
            }

            // destructiveMulAdd
            long ylong = 1000000000 & LONG_MASK;
            long zlong = groupVal & LONG_MASK;

            long product = 0;
            long carry = 0;
            for (int i = 3; i >= 0; i--) {
                switch (i) {
                    case 0:
                        product = ylong * (mag0 & LONG_MASK) + carry;
                        mag0 = (int) product;
                        break;
                    case 1:
                        product = ylong * (mag1 & LONG_MASK) + carry;
                        mag1 = (int) product;
                        break;
                    case 2:
                        product = ylong * (mag2 & LONG_MASK) + carry;
                        mag2 = (int) product;
                        break;
                    case 3:
                        product = ylong * (mag3 & LONG_MASK) + carry;
                        mag3 = (int) product;
                        break;
                    default:
                        throw new ArithmeticException("BigInteger would overflow supported range");
                }
                carry = product >>> 32;
            }

            long sum = (mag3 & LONG_MASK) + zlong;
            mag3 = (int) sum;

            // Perform the addition
            carry = sum >>> 32;
            for (int i = 2; i >= 0; i--) {
                switch (i) {
                    case 0:
                        sum = (mag0 & LONG_MASK) + carry;
                        mag0 = (int) sum;
                        break;
                    case 1:
                        sum = (mag1 & LONG_MASK) + carry;
                        mag1 = (int) sum;
                        break;
                    case 2:
                        sum = (mag2 & LONG_MASK) + carry;
                        mag2 = (int) sum;
                        break;
                    case 3:
                        sum = (mag3 & LONG_MASK) + carry;
                        mag3 = (int) sum;
                        break;
                    default:
                        throw new ArithmeticException("BigInteger would overflow supported range");
                }
                carry = sum >>> 32;
            }
        }
    }

    public interface AutoTypeBeforeHandler
            extends Filter {
        Class<?> apply(String typeName, Class<?> expectClass, long features);
    }

    public static AutoTypeBeforeHandler autoTypeFilter(String... names) {
        return new ContextAutoTypeBeforeHandler(names);
    }

    public static class Context {
        String dateFormat;
        boolean formatyyyyMMddhhmmss19;
        boolean formatyyyyMMddhhmmssT19;
        boolean formatyyyyMMdd8;
        boolean formatMillis;
        boolean formatUnixTime;
        boolean formatISO8601;
        boolean formatHasDay;
        boolean formatHasHour;
        boolean useSimpleFormatter;
        DateTimeFormatter dateFormatter;
        ZoneId zoneId;
        long features;
        Locale locale;
        TimeZone timeZone;
        Supplier<Map> objectSupplier;
        Supplier<List> arraySupplier;
        AutoTypeBeforeHandler autoTypeBeforeHandler;

        protected final ObjectReaderProvider provider;
        protected final SymbolTable symbolTable;

        public Context(ObjectReaderProvider provider) {
            this.features = defaultReaderFeatures;
            this.provider = provider;
            this.symbolTable = null;
        }

        public Context(ObjectReaderProvider provider, SymbolTable symbolTable) {
            this.features = defaultReaderFeatures;
            this.provider = provider;
            this.symbolTable = symbolTable;
        }

        public ObjectReader getObjectReader(Type type) {
            boolean fieldBased = (features & Feature.FieldBased.mask) != 0;
            return provider.getObjectReader(type, fieldBased);
        }

        public ObjectReaderProvider getProvider() {
            return provider;
        }

        public ObjectReader getObjectReaderAutoType(long hashCode) {
            return provider.getObjectReader(hashCode);
        }

        public ObjectReader getObjectReaderAutoType(String typeName, Class expectClass) {
            if (autoTypeBeforeHandler != null && !ObjectReaderProvider.SAFE_MODE) {
                Class<?> autoTypeClass = autoTypeBeforeHandler.apply(typeName, expectClass, features);
                if (autoTypeClass != null) {
                    boolean fieldBased = (features & Feature.FieldBased.mask) != 0;
                    return provider.getObjectReader(autoTypeClass, fieldBased);
                }
            }

            return provider.getObjectReader(typeName, expectClass, features);
        }

        public AutoTypeBeforeHandler getContextAutoTypeBeforeHandler() {
            return autoTypeBeforeHandler;
        }

        public ObjectReader getObjectReaderAutoType(String typeName, Class expectClass, long features) {
            if (autoTypeBeforeHandler != null && !ObjectReaderProvider.SAFE_MODE) {
                Class<?> autoTypeClass = autoTypeBeforeHandler.apply(typeName, expectClass, features);
                if (autoTypeClass != null) {
                    boolean fieldBased = (features & Feature.FieldBased.mask) != 0;
                    return provider.getObjectReader(autoTypeClass, fieldBased);
                }
            }

            return provider.getObjectReader(typeName, expectClass, this.features | features);
        }

        public Supplier<Map> getObjectSupplier() {
            return objectSupplier;
        }

        public void setObjectSupplier(Supplier<Map> objectSupplier) {
            this.objectSupplier = objectSupplier;
        }

        public Supplier<List> getArraySupplier() {
            return arraySupplier;
        }

        public void setArraySupplier(Supplier<List> arraySupplier) {
            this.arraySupplier = arraySupplier;
        }

        public DateTimeFormatter getDateFormatter() {
            if (dateFormatter == null && dateFormat != null && !formatMillis && !formatISO8601 && !formatUnixTime) {
                dateFormatter = locale == null
                        ? DateTimeFormatter.ofPattern(dateFormat)
                        : DateTimeFormatter.ofPattern(dateFormat, locale);
            }
            return dateFormatter;
        }

        public void setDateFormatter(DateTimeFormatter dateFormatter) {
            this.dateFormatter = dateFormatter;
        }

        public String getDateFormat() {
            return dateFormat;
        }

        public void setDateFormat(String format) {
            if (format != null) {
                if (format.isEmpty()) {
                    format = null;
                }
            }

            boolean formatUnixTime = false, formatISO8601 = false, formatMillis = false, hasDay = false, hasHour = false, useSimpleFormatter = false;
            if (format != null) {
                switch (format) {
                    case "unixtime":
                        formatUnixTime = true;
                        break;
                    case "iso8601":
                        formatISO8601 = true;
                        break;
                    case "millis":
                        formatMillis = true;
                        break;
                    case "yyyyMMddHHmmssSSSZ":
                        useSimpleFormatter = true;
                    case "yyyy-MM-dd HH:mm:ss":
                    case "yyyy-MM-ddTHH:mm:ss":
                        formatyyyyMMddhhmmss19 = true;
                        hasDay = true;
                        hasHour = true;
                        break;
                    case "yyyy-MM-dd'T'HH:mm:ss":
                        formatyyyyMMddhhmmssT19 = true;
                        hasDay = true;
                        hasHour = true;
                        break;
                    case "yyyy-MM-dd":
                        formatyyyyMMdd8 = true;
                        hasDay = true;
                        hasHour = false;
                        break;
                    default:
                        hasDay = format.indexOf('d') != -1;
                        hasHour = format.indexOf('H') != -1
                                || format.indexOf('h') != -1
                                || format.indexOf('K') != -1
                                || format.indexOf('k') != -1;
                        break;
                }
            }

            if (!Objects.equals(this.dateFormat, format)) {
                this.dateFormatter = null;
            }
            this.dateFormat = format;
            this.formatUnixTime = formatUnixTime;
            this.formatMillis = formatMillis;
            this.formatISO8601 = formatISO8601;

            this.formatHasDay = hasDay;
            this.formatHasHour = hasHour;
            this.useSimpleFormatter = useSimpleFormatter;
        }

        public ZoneId getZoneId() {
            if (zoneId == null) {
                zoneId = DEFAULT_ZONE_ID;
            }
            return zoneId;
        }

        public long getFeatures() {
            return features;
        }

        public void setZoneId(ZoneId zoneId) {
            this.zoneId = zoneId;
        }

        public Locale getLocale() {
            return locale;
        }

        public void setLocale(Locale locale) {
            this.locale = locale;
        }

        public TimeZone getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(TimeZone timeZone) {
            this.timeZone = timeZone;
        }

        public void config(Feature... features) {
            for (Feature feature : features) {
                this.features |= feature.mask;
            }
        }

        public void config(Filter filter, Feature... features) {
            if (filter instanceof AutoTypeBeforeHandler) {
                autoTypeBeforeHandler = (AutoTypeBeforeHandler) filter;
            }

            for (Feature feature : features) {
                this.features |= feature.mask;
            }
        }

        public void config(Filter[] filters, Feature... features) {
            for (Filter filter : filters) {
                if (filter instanceof AutoTypeBeforeHandler) {
                    autoTypeBeforeHandler = (AutoTypeBeforeHandler) filter;
                }
            }

            for (Feature feature : features) {
                this.features |= feature.mask;
            }
        }

        public boolean isEnabled(Feature feature) {
            return (this.features & feature.mask) != 0;
        }

        public void config(Feature feature, boolean state) {
            if (state) {
                features |= feature.mask;
            } else {
                features &= ~feature.mask;
            }
        }
    }

    public enum Feature {
        FieldBased(1),
        IgnoreNoneSerializable(1 << 1),
        SupportArrayToBean(1 << 2),
        InitStringFieldAsEmpty(1 << 3),
        SupportAutoType(1 << 4),
        SupportSmartMatch(1 << 5),
        UseNativeObject(1 << 6),
        SupportClassForName(1 << 7),
        IgnoreSetNullValue(1 << 8),
        UseDefaultConstructorAsPossible(1 << 9),
        UseBigDecimalForFloats(1 << 10),
        UseBigDecimalForDoubles(1 << 11),
        ErrorOnEnumNotMatch(1 << 12),
        TrimString(1 << 13),
        ErrorOnNotSupportAutoType(1 << 14),
        DuplicateKeyValueAsArray(1 << 15),
        AllowUnQuotedFieldNames(1 << 16),
        NonStringKeyAsString(1 << 17);

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }
    }

    static class ResolveTask {
        final FieldReader fieldReader;
        final Object object;
        final Object name;
        final JSONPath reference;

        ResolveTask(FieldReader fieldReader, Object object, Object name, JSONPath reference) {
            this.fieldReader = fieldReader;
            this.object = object;
            this.name = name;
            this.reference = reference;
        }

        @Override
        public String toString() {
            return reference.toString();
        }
    }

    static LocalDateTime getLocalDateTime(
            char y0,
            char y1,
            char y2,
            char y3,
            char m0,
            char m1,
            char d0,
            char d1,
            char h0,
            char h1,
            char i0,
            char i1,
            char s0,
            char s1,
            char S0,
            char S1,
            char S2,
            char S3,
            char S4,
            char S5,
            char S6,
            char S7,
            char S8) {
        int year;
        if (y0 >= '0' && y0 <= '9'
                && y1 >= '0' && y1 <= '9'
                && y2 >= '0' && y2 <= '9'
                && y3 >= '0' && y3 <= '9'
        ) {
            year = (y0 - '0') * 1000 + (y1 - '0') * 100 + (y2 - '0') * 10 + (y3 - '0');
        } else {
            return null;
        }

        int month;
        if (m0 >= '0' && m0 <= '9'
                && m1 >= '0' && m1 <= '9'
        ) {
            month = (m0 - '0') * 10 + (m1 - '0');
        } else {
            return null;
        }

        int dom;
        if (d0 >= '0' && d0 <= '9'
                && d1 >= '0' && d1 <= '9'
        ) {
            dom = (d0 - '0') * 10 + (d1 - '0');
        } else {
            return null;
        }

        int hour;
        if (h0 >= '0' && h0 <= '9'
                && h1 >= '0' && h1 <= '9'
        ) {
            hour = (h0 - '0') * 10 + (h1 - '0');
        } else {
            return null;
        }

        int minute;
        if (i0 >= '0' && i0 <= '9'
                && i1 >= '0' && i1 <= '9'
        ) {
            minute = (i0 - '0') * 10 + (i1 - '0');
        } else {
            return null;
        }

        int second;
        if (s0 >= '0' && s0 <= '9'
                && s1 >= '0' && s1 <= '9'
        ) {
            second = (s0 - '0') * 10 + (s1 - '0');
        } else {
            return null;
        }

        int nanos;
        if (S0 >= '0' && S0 <= '9'
                && S1 >= '0' && S1 <= '9'
                && S2 >= '0' && S2 <= '9'
                && S3 >= '0' && S3 <= '9'
                && S4 >= '0' && S4 <= '9'
                && S5 >= '0' && S5 <= '9'
                && S6 >= '0' && S6 <= '9'
                && S7 >= '0' && S7 <= '9'
                && S8 >= '0' && S8 <= '9'
        ) {
            nanos = (S0 - '0') * 1000_000_00
                    + (S1 - '0') * 1000_000_0
                    + (S2 - '0') * 1000_000
                    + (S3 - '0') * 1000_00
                    + (S4 - '0') * 1000_0
                    + (S5 - '0') * 1000
                    + (S6 - '0') * 100
                    + (S7 - '0') * 10
                    + (S8 - '0');
        } else {
            return null;
        }

        LocalDate date = LocalDate.of(year, month, dom);
        LocalTime time = LocalTime.of(hour, minute, second, nanos);
        return LocalDateTime.of(date, time);
    }

    protected ZoneId getZoneId(LocalDateTime ldt, String zoneIdStr) {
        ZoneId zoneId;

        int p0, p1;
        if (zoneIdStr != null) {
            if ("000".equals(zoneIdStr)) {
                zoneId = UTC;
            } else if ((p0 = zoneIdStr.indexOf('[')) > 0 && (p1 = zoneIdStr.indexOf(']', p0)) > 0) {
                String str = zoneIdStr.substring(p0 + 1, p1);
                zoneId = ZoneId.of(str);
            } else {
                zoneId = ZoneId.of(zoneIdStr);
            }
        } else {
            zoneId = context.getZoneId();
        }
        return zoneId;
    }

    public String info() {
        return info(null);
    }

    public String info(String message) {
        if (message == null || message.isEmpty()) {
            return "offset " + offset;
        }
        return message + ", offset " + offset;
    }
}
