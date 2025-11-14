package com.yupi.yuaiagent.app.config;

/**
 * LoveApp 应用常量
 * 集中管理所有常量，避免魔法值
 */
public final class LoveAppConstants {
    
    private LoveAppConstants() {
        throw new UnsupportedOperationException("常量类不能实例化");
    }
    
    // ==================== 模板相关 ====================
    
    /** 系统提示词模板路径 */
    public static final String SYSTEM_TEMPLATE_PATH = "promptTemplate/SystemTemplate";
    
    /** 默认顾问名称 */
    public static final String DEFAULT_ADVISOR_NAME = "心灵导师小爱";
    
    /** 默认职业 */
    public static final String DEFAULT_PROFESSION = "程序员";
    
    /** 默认用户名 */
    public static final String DEFAULT_USER_NAME = "用户";
    
    /** 默认问题类型 */
    public static final String DEFAULT_PROBLEM_TYPE = "情感咨询";
    
    /** 默认语气 */
    public static final String DEFAULT_TONE = "温暖而专业";
    
    /** 默认最大字数 */
    public static final String DEFAULT_MAX_WORDS = "300";
    
    // ==================== 用户状态 ====================
    
    /** 单身状态 */
    public static final String STATUS_SINGLE = "单身";
    
    /** 恋爱状态 */
    public static final String STATUS_DATING = "恋爱";
    
    /** 已婚状态 */
    public static final String STATUS_MARRIED = "已婚";
    
    // ==================== 报告生成 ====================
    
    /** 报告生成格式说明 */
    public static final String REPORT_FORMAT_INSTRUCTION = """
           你必须生成符合下面格式的回答：
           使用Json格式，例如
            {"title": "恋爱报告：程序员鱼皮的爱情指南",
              "suggestions": [
                   "拓展社交圈：积极参与社区活动、线上社群，扩大社交圈子。",
                   "提升个人魅力：注重仪容仪表、提升内在修养，展现个人魅力。",
                    "培养共同兴趣：找到共同爱好，增加互动和话题，培养情感联系。",
                  "练习沟通技巧：清晰表达需求，积极倾听，表达真挚情感。",
                  "寻求专业帮助：考虑心理咨询师，更好地了解自我和爱情。",
                ]
             }
            """;
    
    /** 报告生成附加指令 */
    public static final String REPORT_GENERATION_INSTRUCTION = 
            "\n\n每次对话后都要生成恋爱结果，标题为用户名的恋爱报告，内容为建议列表。\n";
}
