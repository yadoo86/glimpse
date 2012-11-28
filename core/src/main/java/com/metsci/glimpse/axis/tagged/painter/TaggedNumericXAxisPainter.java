/*
 * Copyright (c) 2012, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.axis.tagged.painter;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.NumericXAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * A horizontal (x) axis painter which displays positions of tags in addition
 * to tick marks and labels. This axis must be added to a
 * {@link com.metsci.glimpse.layout.GlimpseAxisLayout1D} whose associated
 * axis is a {@link com.metsci.glimpse.axis.tagged.TaggedAxis1D}.
 *
 * @author ulman
 */
public class TaggedNumericXAxisPainter extends NumericXAxisPainter
{
    protected static final int DEFAULT_TAG_HEIGHT = 8;
    protected static final int DEFAULT_TAG_BASE = 8;

    protected float[] tagColor = GlimpseColor.fromColorRgba( 0.0f, 0.0f, 0.0f, 0.2f );
    protected boolean tagColorSet = false;
    
    protected int tagWidth = DEFAULT_TAG_BASE;
    protected int tagHeight = DEFAULT_TAG_HEIGHT;

    public TaggedNumericXAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );
    }

    public void setTagColor( float[] color )
    {
        this.tagColor = color;
    }

    public void setTagWidth( int width )
    {
        this.tagWidth = width;
    }

    public void setTagHeight( int height )
    {
        this.tagHeight = height;
    }
    
    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        super.setLookAndFeel( laf );
        
        if ( !tagColorSet )
        {
            setTagColor( laf.getColor( AbstractLookAndFeel.AXIS_TAG_COLOR ) );
            tagColorSet = false;
        }
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis1D axis )
    {
        if ( axis instanceof TaggedAxis1D )
        {
            TaggedAxis1D taggedAxis = (TaggedAxis1D) axis;

            GL gl = context.getGL( );

            int width = bounds.getWidth( );
            int height = bounds.getHeight( );

            gl.glMatrixMode( GL.GL_PROJECTION );
            gl.glLoadIdentity( );
            gl.glOrtho( -0.5, width - 1 + 0.5f, -0.5, height - 1 + 0.5f, -1, 1 );

            gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
            gl.glEnable( GL.GL_BLEND );

            paintTicks( gl, taggedAxis, width, height );
            paintSelectionLine( gl, taggedAxis, width, height );

            paintTags( gl, taggedAxis, width, height );
        }
    }

    protected void paintTags( GL gl, TaggedAxis1D taggedAxis, int width, int height )
    {
        for ( Tag tag : taggedAxis.getSortedTags( ) )
        {
            paintTag( gl, tag, taggedAxis, width, height );
        }
    }

    protected void paintTag( GL gl, Tag tag, TaggedAxis1D taggedAxis, int width, int height )
    {
        int x = taggedAxis.valueToScreenPixel( tag.getValue( ) );
        int y1 = height - 1 - tickBufferSize - tagHeight;
        int y2 = y1 + tagHeight;

        GlimpseColor.glColor( gl, tagColor );
        gl.glBegin( GL.GL_TRIANGLES );
        try
        {
            gl.glVertex2f( x, y2 );
            gl.glVertex2f( x - tagWidth / 2.0f, y1 );
            gl.glVertex2f( x + tagWidth / 2.0f, y1 );
        }
        finally
        {
            gl.glEnd( );
        }
    }
}
