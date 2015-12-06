/*
 * Copyright (C) 2015 Redjan Shabani
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package seatscalculator;

import java.util.Objects;

/**
 *
 * @author Redjan Shabani
 */
public class Subject
{
	public static final int COALITION = 0, PARTY = 1, INDIVIDUAL = 2;
	
	private final int category;
	private final String name;
	private final int votes;
	private float percentage;
	private float idealSeats;
	private int realSeats;

	public Subject(int category, String name, int votes)
	{
		this.category = category;
		this.name = name;
		this.votes = votes;
	}

	public int getCategory()
	{
		return category;
	}

	public String getName()
	{
		return name;
	}

	public int getVotes()
	{
		return votes;
	}

	public float getPercentage()
	{
		return percentage;
	}

	public float getIdealSeats()
	{
		return idealSeats;
	}

	public int getRealSeats()
	{
		return realSeats;
	}

	public void setPercentage(float percentage)
	{
		this.percentage = percentage;
	}

	public void setIdealSeats(float idealSeats)
	{
		this.idealSeats = idealSeats;
	}

	public void setRealSeats(int realSeats)
	{
		this.realSeats = realSeats;
	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 19 * hash + Objects.hashCode(this.name);
		return hash;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final Subject other = (Subject) obj;
		if (!Objects.equals(this.name, other.name))
		{
			return false;
		}
		return true;
	}
	
	
}
