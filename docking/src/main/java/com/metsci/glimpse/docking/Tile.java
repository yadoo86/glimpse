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
package com.metsci.glimpse.docking;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import static com.metsci.glimpse.docking.DockingUtils.*;
import static com.metsci.glimpse.docking.MiscUtils.*;
import static java.awt.BasicStroke.*;
import static java.lang.Math.*;
import static javax.swing.BorderFactory.*;
import static javax.swing.SwingConstants.*;

public class Tile extends JComponent
{


    protected class CustomTab extends JPanel
    {
        protected final int lineThickness = theme.lineThickness;
        protected final int cornerRadius = theme.cornerRadius;
        protected final int labelPadding = theme.labelPadding;

        protected final Color lineColor = theme.lineColor;
        protected final Color highlightColor = theme.highlightColor;
        protected final Color selectedTextColor = theme.selectedTextColor;
        protected final Color unselectedTextColor = theme.unselectedTextColor;

        protected final View view;
        protected boolean selected;
        protected final JLabel label;

        public CustomTab( View view )
        {
            setLayout( new BorderLayout( ) );

            setOpaque( false );
            setBorder( null );
            setToolTipText( view.tooltip );

            this.view = view;
            this.selected = false;
            this.label = new JLabel( view.title, view.icon, LEFT );
            label.setForeground( unselectedTextColor );

            // Add extra space on top and right, because text is right up against the edge
            label.setBorder( createEmptyBorder( 2, lineThickness + labelPadding, 0, labelPadding + 2 ) );

            add( label, BorderLayout.CENTER );
        }

        public void setSelected( boolean selected )
        {
            if ( selected != this.selected )
            {
                this.selected = selected;
                label.setForeground( selected ? selectedTextColor : unselectedTextColor );
            }
        }

        @Override
        public Dimension getPreferredSize( )
        {
            Dimension d = super.getPreferredSize( );

            boolean rightmost = ( views.indexOf( view ) == rightmostViewNum( ) );
            if ( rightmost ) d.width += lineThickness;

            return d;
        }

        @Override
        public Dimension getMinimumSize( )
        {
            Dimension d = super.getMinimumSize( );

            boolean rightmost = ( views.indexOf( view ) == rightmostViewNum( ) );
            if ( rightmost ) d.width += lineThickness;

            return d;
        }

        @Override
        protected void paintComponent( Graphics g0 )
        {
            Graphics2D g = ( Graphics2D ) g0;

            int viewNum = views.indexOf( view );
            int selectedViewNum = views.indexOf( selectedView );
            int leftmostViewNum = leftmostViewNum( );
            int rightmostViewNum = rightmostViewNum( );
            boolean selected = ( viewNum == selectedViewNum );
            boolean leftmost = ( viewNum == leftmostViewNum );
            boolean rightmost = ( viewNum == rightmostViewNum );

            int wBox = ( rightmost ? getWidth( ) : getWidth( ) + lineThickness );
            int hBox = getHeight( );


            // Fill
            if ( selected )
            {
                Color bottomColor = new Color( highlightColor.getRed( ), highlightColor.getGreen( ), highlightColor.getBlue( ), 0 );
                Color topColor = ( viewNum == selectedViewNum ? highlightColor : bottomColor );
                g.setPaint( new GradientPaint( 0, 0, topColor, 0, getHeight( ) - 1, bottomColor ) );

                g.fillRoundRect( lineThickness/2, lineThickness/2, wBox - lineThickness, hBox + cornerRadius, cornerRadius, cornerRadius );
            }


            // Edge lines
            g.setPaint( lineColor );
            g.setStroke( new BasicStroke( lineThickness ) );


            // Top edge
            if ( leftmost )
            {
                g.drawLine( cornerRadius, lineThickness/2, wBox - 1, lineThickness/2 );
            }
            else
            {
                g.drawLine( 0, lineThickness/2, wBox - 1, lineThickness/2 );
            }


            // Side edges
            if ( selected )
            {
                g.drawRoundRect( lineThickness/2, lineThickness/2, wBox - lineThickness, hBox + cornerRadius, cornerRadius, cornerRadius );
            }
            else
            {
                // Left edge
                if ( leftmost )
                {
                    g.drawRoundRect( lineThickness/2, lineThickness/2, wBox + cornerRadius, hBox + cornerRadius, cornerRadius, cornerRadius );
                }
                else if ( viewNum == selectedViewNum + 1 )
                {
                    g.drawRoundRect( lineThickness/2 - ( wBox - lineThickness ), lineThickness/2, wBox - lineThickness, hBox + cornerRadius, cornerRadius, cornerRadius );
                }
                else
                {
                    g.drawLine( lineThickness/2, 0, lineThickness/2, hBox - 1 );
                }

                // Right edge
                if ( rightmost )
                {
                    g.drawLine( wBox - 1 - lineThickness/2, 0, wBox - 1 - lineThickness/2, hBox - 1 );
                }
            }
        }
    }




    protected final DockingTheme theme;

    protected final JPanel topBar;

    protected final JToolBar tabBar;

    protected final JToolBar overflowBar;
    protected final JToggleButton overflowPopupButton;
    protected final JPopupMenu overflowPopup;

    protected final JToolBar cornerBar;

    protected final JPanel viewBarHolder;

    protected final CardLayout cardLayout;
    protected final JPanel cardPanel;

    protected final List<MouseAdapter> dockingMouseAdapters;

    protected final Map<ViewKey,ViewEntry> viewMap;
    protected final List<View> views;
    protected View selectedView;


    public Tile( DockingTheme theme, Component... cornerComponents )
    {
        this.theme = theme;
        final int lineThickness = theme.lineThickness;
        final int cornerRadius = theme.cornerRadius;
        final int cardPadding = theme.cardPadding;
        final Color lineColor = theme.lineColor;

        this.tabBar = newToolbar( false );
        tabBar.setBorder( null );

        this.overflowBar = newToolbar( true );
        this.overflowPopupButton = new JToggleButton( "\u00BB" );
        this.overflowPopup = newButtonPopup( overflowPopupButton );
        overflowBar.add( overflowPopupButton );

        this.cornerBar = newToolbar( true );
        for ( Component c : cornerComponents ) cornerBar.add( c );

        this.viewBarHolder = new JPanel( new GridLayout( 1, 1 ) );

        this.cardLayout = new CardLayout( );
        this.cardPanel = new JPanel( cardLayout );
        cardPanel.setBorder( createCompoundBorder( createMatteBorder( 0, lineThickness, lineThickness, lineThickness, lineColor ),
                                                   createEmptyBorder( cardPadding, cardPadding, cardPadding, cardPadding ) ) );

        this.dockingMouseAdapters = newArrayList( );


        this.viewMap = newHashMap( );
        this.views = newArrayList( );
        this.selectedView = null;


        this.topBar = new JPanel( )
        {
            protected void paintComponent( Graphics g0 )
            {
                Graphics2D g = ( Graphics2D ) g0;
                super.paintComponent( g );

                // Certain L&Fs get messed up if we don't reset the stroke back how we found it
                Stroke origStroke = g.getStroke( );

                g.setColor( lineColor );
                g.setStroke( new BasicStroke( lineThickness, CAP_BUTT, JOIN_MITER ) );
                g.drawRoundRect( lineThickness/2, lineThickness/2, getWidth( ) - lineThickness, getHeight( ) + cornerRadius, cornerRadius, cornerRadius );

                for ( View view : views )
                {
                    if ( view != selectedView )
                    {
                        CustomTab tab = viewMap.get( view.viewKey ).tab;

                        // CAP_BUTT behaves differently when line-width is 1
                        int wExtra = ( lineThickness > 1 ? lineThickness : 0 );

                        g.drawLine( tab.getX( ), getHeight( ) - 1 - lineThickness/2, tab.getX( ) + tab.getWidth( ) + wExtra, getHeight( ) - 1 - lineThickness/2 );
                    }
                }

                g.drawLine( tabBar.getWidth( ) - lineThickness, getHeight( ) - 1 - lineThickness/2, getWidth( ) - 1, getHeight( ) - 1 - lineThickness/2 );

                g.setStroke( origStroke );
            }
        };
        topBar.setOpaque( true );

        topBar.add( tabBar );
        topBar.add( overflowBar );
        topBar.add( viewBarHolder );
        topBar.add( cornerBar );

        topBar.setLayout( new LayoutManager( )
        {
            public void layoutContainer( Container parent )
            {

                int wTotal = topBar.getWidth( ) - cornerRadius;
                int wCornerBar = cornerBar.getMinimumSize( ).width;
                int wOverflowBar = overflowBar.getMinimumSize( ).width;

                for ( View view : views )
                {
                    ViewEntry viewEntry = viewMap.get( view.viewKey );
                    viewEntry.overflowMenuItem.setVisible( false );
                    viewEntry.tab.setVisible( true );
                }
                overflowBar.setVisible( false );


                boolean needsOverflow = false;
                while ( true )
                {
                    tabBar.doLayout( );
                    int wTabBar = tabBar.getMinimumSize( ).width;
                    int wAvail = wTotal - wCornerBar - ( needsOverflow ? wOverflowBar : 0 );
                    if ( wTabBar <= wAvail ) break;

                    int numVisible = 0;
                    ViewEntry firstVisible = null;
                    ViewEntry lastVisible = null;
                    for ( View view : views )
                    {
                        ViewEntry viewEntry = viewMap.get( view.viewKey );
                        if ( viewEntry.tab.isVisible( ) )
                        {
                            numVisible++;
                            lastVisible = viewEntry;
                            if ( firstVisible == null ) firstVisible = viewEntry;
                        }
                    }
                    if ( numVisible <= 1 ) break;

                    ViewEntry victim = ( lastVisible.view == selectedView ? firstVisible : lastVisible );
                    victim.overflowMenuItem.setVisible( true );
                    victim.tab.setVisible( false );
                    needsOverflow = true;
                }


                int y = 0;
                int hTotal = topBar.getHeight( ) - lineThickness;

                int xTabBar = 0;
                int wTabBar = tabBar.getMinimumSize( ).width;
                tabBar.setBounds( xTabBar, y, wTabBar, hTotal );

                if ( needsOverflow )
                {
                    int xOverflowBar = tabBar.getX( ) + tabBar.getWidth( );
                    overflowBar.setBounds( xOverflowBar, y + lineThickness, wOverflowBar, hTotal - lineThickness );
                    overflowBar.setVisible( true );
                }

                int xCornerBar = wTotal - wCornerBar;
                cornerBar.setBounds( xCornerBar, y + lineThickness, wCornerBar, hTotal - lineThickness );


                viewBarHolder.removeAll( );

                if ( selectedView == null || selectedView.toolbar == null )
                {
                    viewBarHolder.setVisible( false );
                }
                else
                {
                    JPanel viewCard = viewMap.get( selectedView.viewKey ).card;

                    JToolBar viewBar = selectedView.toolbar;
                    int xViewBar = xTabBar + wTabBar + ( needsOverflow ? wOverflowBar : 0 );
                    int wViewBar = xCornerBar - xViewBar;

                    if ( viewBar.getMinimumSize( ).width <= wViewBar )
                    {
                        viewCard.remove( viewBar );
                        viewBarHolder.add( viewBar );
                        viewBarHolder.setBounds( xViewBar, y + lineThickness, wViewBar, hTotal - lineThickness );
                        viewBarHolder.setVisible( true );
                    }
                    else
                    {
                        viewBarHolder.setVisible( false );
                        viewCard.add( viewBar, BorderLayout.NORTH );
                    }
                }

            }

            public void addLayoutComponent( String name, Component comp )
            { }

            public void removeLayoutComponent( Component comp )
            { }

            public Dimension preferredLayoutSize( Container parent )
            {
                int wTile = getWidth( );
                int hBars = lineThickness + max( tabBar.getPreferredSize( ).height, max( overflowBar.getPreferredSize( ).height + lineThickness, cornerBar.getPreferredSize( ).height + lineThickness ) );
                return new Dimension( wTile, hBars );
            }

            public Dimension minimumLayoutSize( Container parent )
            {
                int wTab = 0;
                for ( View view : views )
                {
                    ViewEntry viewEntry = viewMap.get( view.viewKey );
                    wTab = max( wTab, viewEntry.tab.getMinimumSize( ).width );
                }
                int wBars = wTab + overflowBar.getMinimumSize( ).width + cornerBar.getMinimumSize( ).width + cornerRadius;
                int hBars = lineThickness + max( tabBar.getMinimumSize( ).height, max( overflowBar.getMinimumSize( ).height + lineThickness, cornerBar.getMinimumSize( ).height + lineThickness ) );
                return new Dimension( wBars, hBars );
            }
        } );


        setLayout( new BorderLayout( ) );
        add( topBar, BorderLayout.NORTH );
        add( cardPanel, BorderLayout.CENTER );
    }

    public int numViews( )
    {
        return views.size( );
    }

    public View view( int viewNum )
    {
        return views.get( viewNum );
    }

    public View selectedView( )
    {
        return selectedView;
    }

    public void addView( final View view, int viewNum )
    {
        JPanel card = new JPanel( new BorderLayout( ) );
        card.add( view.component, BorderLayout.CENTER );
        cardPanel.add( card, view.viewKey.viewId );

        CustomTab tab = new CustomTab( view );
        tab.addMouseListener( new MouseAdapter( )
        {
            public void mousePressed( MouseEvent ev )
            {
                selectView( view );
            }
        } );
        for ( MouseAdapter mouseAdapter : dockingMouseAdapters )
        {
            addMouseAdapter( tab, mouseAdapter );
        }
        tabBar.add( tab, viewNum );

        JMenuItem overflowMenuItem = new JMenuItem( view.title, view.icon );
        overflowMenuItem.setToolTipText( view.tooltip );
        overflowMenuItem.addActionListener( new ActionListener( )
        {
            public void actionPerformed( ActionEvent ev )
            {
                selectView( view );
            }
        } );
        overflowPopup.add( overflowMenuItem );

        viewMap.put( view.viewKey, new ViewEntry( view, card, tab, overflowMenuItem ) );
        views.add( viewNum, view );

        if ( selectedView == null )
        {
            selectView( view );
        }
    }

    public void removeView( View view )
    {
        boolean removingSelectedView = ( view == selectedView );

        if ( removingSelectedView )
        {
            selectView( null );
        }

        ViewEntry viewEntry = viewMap.remove( view.viewKey );
        overflowPopup.remove( viewEntry.overflowMenuItem );
        tabBar.remove( viewEntry.tab );
        cardPanel.remove( viewEntry.card );
        views.remove( view );

        if ( removingSelectedView && !views.isEmpty( ) )
        {
            selectView( views.get( 0 ) );
        }
    }

    public boolean hasView( View view )
    {
        return ( view != null && viewMap.containsKey( view.viewKey ) );
    }

    public void selectView( View view )
    {
        if ( view == selectedView ) return;

        if ( selectedView != null )
        {
            ViewEntry viewEntry = viewMap.get( selectedView.viewKey );
            viewEntry.tab.setSelected( false );
            cardPanel.setVisible( false );
        }

        if ( view != null )
        {
            ViewEntry viewEntry = viewMap.get( view.viewKey );
            viewEntry.tab.setSelected( true );
            cardLayout.show( cardPanel, view.viewKey.viewId );
            cardPanel.setVisible( true );
        }

        selectedView = view;

        topBar.doLayout( );
        topBar.repaint( );
    }

    public int viewNumForTabAt( int x, int y )
    {
        for ( int viewNum = 0; viewNum < numViews( ); viewNum++ )
        {
            Rectangle tabBounds = tabBounds( viewNum );
            if ( tabBounds != null && tabBounds.contains( x, y ) )
            {
                return viewNum;
            }
        }
        return -1;
    }

    public Rectangle viewTabBounds( int viewNum )
    {
        Rectangle bounds = tabBounds( viewNum );

        bounds.height += theme.lineThickness;

        if ( viewNum != rightmostViewNum( ) )
        {
            bounds.width += theme.lineThickness;
        }

        return bounds;
    }

    protected Rectangle tabBounds( int viewNum )
    {
        CustomTab tab = viewEntry( viewNum ).tab;
        if ( !tab.isVisible( ) ) return null;

        // Tab position relative to tile
        int x = 0;
        int y = 0;
        for ( Component c = tab; c != this; c = c.getParent( ) )
        {
            x += c.getX( );
            y += c.getY( );
        }

        return new Rectangle( x, y, tab.getWidth( ), tab.getHeight( ) );
    }

    protected int leftmostViewNum( )
    {
        for ( int i = 0; i < views.size( ); i++ )
        {
            if ( viewEntry( i ).tab.isVisible( ) )
            {
                return i;
            }
        }
        return -1;
    }

    protected int rightmostViewNum( )
    {
        for ( int i = views.size( ) - 1; i >= 0; i-- )
        {
            if ( viewEntry( i ).tab.isVisible( ) )
            {
                return i;
            }
        }
        return views.size( );
    }

    public void addDockingMouseAdapter( MouseAdapter mouseAdapter )
    {
        for ( View view : views )
        {
            CustomTab tab = viewMap.get( view.viewKey ).tab;
            addMouseAdapter( tab, mouseAdapter );
        }

        this.dockingMouseAdapters.add( mouseAdapter );
    }

    public static void addMouseAdapter( Component c, MouseAdapter mouseAdapter )
    {
        c.addMouseListener( mouseAdapter );
        c.addMouseMotionListener( mouseAdapter );
        c.addMouseWheelListener( mouseAdapter );
    }

    protected ViewEntry viewEntry( int viewNum )
    {
        return viewMap.get( views.get( viewNum ).viewKey );
    }



    protected static class ViewEntry
    {
        public final View view;
        public final JPanel card;
        public final CustomTab tab;
        public final JMenuItem overflowMenuItem;

        public ViewEntry( View view, JPanel card, CustomTab tab, JMenuItem overflowMenuItem )
        {
            this.view = view;
            this.card = card;
            this.tab = tab;
            this.overflowMenuItem = overflowMenuItem;
        }
    }

}
