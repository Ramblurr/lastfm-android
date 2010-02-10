<?xml version='1.0'?> 
<xsl:stylesheet  
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"> 

<xsl:import href="../docbook-xsl/html/chunkfast.xsl"/> 

<xsl:param name="html.stylesheet" select="'style.css'"/> 
<xsl:param name="suppress.navigation" select="0"/>
<xsl:param name="chunk.first.sections" select = "1"/>
<xsl:param name="chunk.quietly" select = "1"/>
<xsl:param name="use.id.as.filename" select = "1"/>

<xsl:template name="user.head.content">
<meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;" />
</xsl:template>

<xsl:template name="header.navigation">
<xsl:param name="prev" select="/foo"/>
<xsl:param name="next" select="/foo"/>
<div id="header">
<span class="headerLinks">
<xsl:if test="count($next)>0">
  <a accesskey="p">
    <xsl:attribute name="href">
      <xsl:call-template name="href.target">
        <xsl:with-param name="object" select="$next"/>
      </xsl:call-template>
    </xsl:attribute>
	&gt;
  </a>
</xsl:if>
<a href="index.html">Home</a>
<xsl:if test="count($prev)>0">
  <a accesskey="p">
    <xsl:attribute name="href">
      <xsl:call-template name="href.target">
        <xsl:with-param name="object" select="$prev"/>
      </xsl:call-template>
    </xsl:attribute>
    &lt;
  </a>
<span class="separator">|</span>
</xsl:if>
</span>
<h1 class="header">
<xsl:apply-templates select="." mode="object.title.markup"/>
</h1>
</div>
</xsl:template>

<xsl:template name="footer.navigation">
<xsl:param name="prev" select="/foo"/>
<xsl:param name="next" select="/foo"/>
<table width="100%" style="background: white; border: none;">
<tr>
<xsl:if test="count($prev)>0">
<td align="left">
  <a accesskey="p">
    <xsl:attribute name="href">
      <xsl:call-template name="href.target">
        <xsl:with-param name="object" select="$prev"/>
      </xsl:call-template>
    </xsl:attribute>
    &lt; Previous
	<!-- <xsl:apply-templates select="$prev" mode="object.title.markup"/> -->
  </a>
</td>
</xsl:if>
<xsl:if test="count($next)>0">
<td align="right">
  <a accesskey="p">
    <xsl:attribute name="href">
      <xsl:call-template name="href.target">
        <xsl:with-param name="object" select="$next"/>
      </xsl:call-template>
    </xsl:attribute>
	<!-- <xsl:apply-templates select="$next" mode="object.title.markup"/> -->
	Next &gt;
  </a>
</td>
</xsl:if>
</tr>
</table>
<div id="footer">
    <small>
        © 2010 Last.fm Ltd.
    </small>
</div>
</xsl:template>

<xsl:template name="body.attributes">
</xsl:template>

</xsl:stylesheet>